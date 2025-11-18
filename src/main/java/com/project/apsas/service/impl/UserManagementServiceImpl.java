package com.project.apsas.service.impl;

import com.project.apsas.dto.request.admin.CreateUserRequest;
import com.project.apsas.dto.request.admin.UpdateUserRoleRequest;
import com.project.apsas.dto.request.admin.UpdateUserStatusRequest;
import com.project.apsas.dto.response.admin.UserManagementResponse;
import com.project.apsas.dto.response.admin.UserStatisticsResponse;
import com.project.apsas.entity.Profile;
import com.project.apsas.entity.Progress;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.exception.AppException;
import com.project.apsas.exception.ErrorCode;
import com.project.apsas.repository.ProgressRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;
import com.project.apsas.service.UserManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ProgressRepository progressRepository;

    @Override
    public Page<UserManagementResponse> getAllUsers(Pageable pageable, String search, UserStatus status, String role) {
        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name or email
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase(Locale.ROOT) + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                predicates.add(criteriaBuilder.or(namePredicate, emailPredicate));
            }

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by role
            if (role != null && !role.trim().isEmpty()) {
                predicates.add(criteriaBuilder.isMember(
                        role.toUpperCase(Locale.ROOT),
                        root.join("roles").get("name")
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToUserManagementResponse);
    }

    @Override
    public UserManagementResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return mapToUserManagementResponse(user);
    }

    @Override
    public UserManagementResponse createUser(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase(Locale.ROOT))) {
            throw new AppException(ErrorCode.USER_ESIXSTED);
        }

        // Get roles
        Set<Role> roles = request.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        // Create user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus())
                .roles(roles)
                .build();

        // Create profile
        Profile profile = Profile.builder()
                .user(user)
                .build();
        user.setProfile(profile);

        // Save user first (without progress)
        User savedUser = userRepository.save(user);
        
        // Create progress for students AFTER user is saved (to get user ID)
        boolean hasStudentRole = roles.stream()
                .anyMatch(r -> "STUDENT".equals(r.getName()));
        if (hasStudentRole) {
            Progress progress = Progress.builder()
                    .userId(savedUser.getId())
                    .totalAttemptNo(0)
                    .acceptance(0.0f)
                    .build();
            progressRepository.save(progress);
            log.info("Created progress for new student user: {}", savedUser.getEmail());
        }

        log.info("Admin created new user: {} with roles: {}", savedUser.getEmail(), 
                roles.stream().map(Role::getName).collect(Collectors.joining(", ")));

        return mapToUserManagementResponse(savedUser);
    }

    @Override
    public UserManagementResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setStatus(request.getStatus());
        User updatedUser = userRepository.save(user);

        log.info("Admin updated user {} status to: {}", user.getEmail(), request.getStatus());
        return mapToUserManagementResponse(updatedUser);
    }

    @Override
    public UserManagementResponse updateUserRoles(Long userId, UpdateUserRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Get new roles
        Set<Role> newRoles = request.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());

        // Check if student role is added/removed to handle progress
        boolean hadStudentRole = user.getRoles().stream()
                .anyMatch(r -> "STUDENT".equals(r.getName()));
        boolean hasStudentRole = newRoles.stream()
                .anyMatch(r -> "STUDENT".equals(r.getName()));

        // Update roles
        user.setRoles(newRoles);

        // Create progress if student role is added
        if (!hadStudentRole && hasStudentRole && user.getProgress() == null) {
            Progress progress = Progress.builder()
                    .userId(user.getId())
                    .totalAttemptNo(0)
                    .acceptance(0.0f)
                    .build();
            progressRepository.save(progress);
            user.setProgress(progress);
        }

        User updatedUser = userRepository.save(user);

        log.info("Admin updated user {} roles to: {}", user.getEmail(),
                newRoles.stream().map(Role::getName).collect(Collectors.joining(", ")));

        return mapToUserManagementResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Cannot delete admin if it's the last admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getName()));
        
        if (isAdmin) {
            long adminCount = userRepository.countByRolesName("ADMIN");
            if (adminCount <= 1) {
                throw new AppException(ErrorCode.CANNOT_DELETE_LAST_ADMIN);
            }
        }

        userRepository.delete(user);
        log.info("Admin deleted user: {}", user.getEmail());
    }

    @Override
    public UserStatisticsResponse getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);
        long unverifiedUsers = userRepository.countByStatus(UserStatus.UNVERIFIED);
        long studentsCount = userRepository.countByRolesName("STUDENT");
        long lecturersCount = userRepository.countByRolesName("LECTURER");
        long adminsCount = userRepository.countByRolesName("ADMIN");

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .blockedUsers(blockedUsers)
                .unverifiedUsers(unverifiedUsers)
                .studentsCount(studentsCount)
                .lecturersCount(lecturersCount)
                .adminsCount(adminsCount)
                .build();
    }

    private UserManagementResponse mapToUserManagementResponse(User user) {
        Set<UserManagementResponse.RoleInfo> roleInfos = user.getRoles().stream()
                .map(role -> UserManagementResponse.RoleInfo.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .collect(Collectors.toSet());

        UserManagementResponse.ProfileInfo profileInfo = null;
        if (user.getProfile() != null) {
            profileInfo = UserManagementResponse.ProfileInfo.builder()
                    .avatarUrl(user.getProfile().getAvatarUrl())
                    .bio(user.getProfile().getBio())
                    .phone(user.getProfile().getPhone())
                    .build();
        }

        return UserManagementResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roles(roleInfos)
                .profile(profileInfo)
                .build();
    }
}
