package com.project.apsas.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.project.apsas.entity.Permission;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import com.project.apsas.enums.UserStatus;
import com.project.apsas.repository.PermissionRepository;
import com.project.apsas.repository.RoleRepository;
import com.project.apsas.repository.UserRepository;

@Component
@Slf4j
public class DataSeeder implements ApplicationRunner {
    private final PermissionRepository permRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final AdminProperties admin;
    @Value("${app.seed.enabled:true}") private boolean seedEnabled;

    public DataSeeder(PermissionRepository p, RoleRepository r, UserRepository u,
                      PasswordEncoder e, AdminProperties a) {
        this.permRepo = p; this.roleRepo = r; this.userRepo = u; this.encoder = e; this.admin = a;
    }

    @Override 
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled) return;
        
        try {
            seedData();
        } catch (Exception e) {
            System.err.println("⚠️ DataSeeder failed (database may already be seeded): " + e.getMessage());
            // Không throw exception để app vẫn start được
        }
    }
    
    private void seedData() {

        try {
            log.info("Starting data seeding...");
            seedPermissions();
            seedRolesAndPermissions();
            seedAdminUser();
            log.info("Data seeding completed successfully");
        } catch (Exception e) {
            log.error("Error during data seeding: {}", e.getMessage(), e);
            // Không throw exception để app vẫn start được
        }
    }

    @Transactional
    protected void seedPermissions() {
        log.info("Seeding permissions...");
        List<String> P = List.of(
                // Admin - User Management
                "VIEW_USERS", "CREATE_USERS", "UPDATE_USERS", "DELETE_USERS",
                // Admin - Role Management
                "VIEW_ROLES", "CREATE_ROLES", "UPDATE_ROLES", "DELETE_ROLES",
                // Admin - Tutorial Management
                "MANAGE_TUTORIALS", "PUBLISH_TUTORIALS",
                // Content Provider - Tutorial Operations
                "CREATE_TUTORIAL", "UPDATE_TUTORIAL", "DELETE_TUTORIAL", "VIEW_OWN_TUTORIALS",
                // Content Provider - Content & Assignment
                "CREATE_CONTENT", "UPDATE_CONTENT", "CREATE_ASSIGNMENT", "UPDATE_ASSIGNMENT",
                // Lecturer - Course Management
                "VIEW_COURSES", "CREATE_COURSE", "UPDATE_COURSE", "DELETE_COURSE",
                // Student - Course Operations
                "ENROLL_COURSE",
                // Teacher - Submission & Evaluation
                "VIEW_SUBMISSIONS", "EVALUATE_SUBMISSIONS",
                // Student - Assignment Operations
                "SUBMIT_ASSIGNMENT",
                // Support & Help
                "VIEW_FEEDBACK", "RESPOND_FEEDBACK", "REQUEST_HELP", "VIEW_HELP_REQUESTS",
                // Notifications
                "VIEW_NOTIFICATIONS", "MANAGE_NOTIFICATIONS",
                // Statistics
                "VIEW_TEACHER_STATS",
                // General
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE",
                "RESOURCE_READ", "RESOURCE_WRITE", "TESTCASE_READ", "TESTCASE_WRITE",
                "SCHEDULE_READ", "NOTIF_READ", "NOTIF_WRITE",
                "USER_MANAGE", "API_MANAGE", "MAINTENANCE_MANAGE", "POLICY_MANAGE"
        );

        for (String name : P) {
            if (!permRepo.findByName(name).isPresent()) {
                try {
                    permRepo.save(Permission.builder()
                            .name(name)
                            .description(name.replace('_', ' ').toLowerCase())
                            .build());
                    log.debug("Created permission: {}", name);
                } catch (Exception e) {
                    log.warn("Permission {} might already exist: {}", name, e.getMessage());
                }
            }
        }
    }

    @Transactional
    protected void seedRolesAndPermissions() {
        log.info("Seeding roles and role-permission mappings...");
        
        Map<String, List<String>> R = new LinkedHashMap<>();
        
        // GUEST - Chỉ xem public content
        R.put("GUEST", List.of("RESOURCE_READ", "SUPPORT_CREATE"));
        
        // STUDENT - Học viên
        R.put("STUDENT", List.of(
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE",
                "VIEW_COURSES", "ENROLL_COURSE", "SUBMIT_ASSIGNMENT",
                "VIEW_NOTIFICATIONS", "REQUEST_HELP", "RESOURCE_READ",
                "SCHEDULE_READ", "SUPPORT_CREATE"
        ));
        
        // LECTURER - Giảng viên
        R.put("LECTURER", List.of(
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE",
                "VIEW_COURSES", "CREATE_COURSE", "UPDATE_COURSE", "DELETE_COURSE",
                "VIEW_SUBMISSIONS", "EVALUATE_SUBMISSIONS", "VIEW_HELP_REQUESTS",
                "RESPOND_FEEDBACK", "VIEW_TEACHER_STATS", "VIEW_NOTIFICATIONS",
                "RESOURCE_READ", "RESOURCE_WRITE", "SUPPORT_CREATE"
        ));
        
        // CONTENT_PROVIDER - Người cung cấp nội dung
        R.put("CONTENT_PROVIDER", List.of(
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE",
                "CREATE_TUTORIAL", "UPDATE_TUTORIAL", "DELETE_TUTORIAL", "VIEW_OWN_TUTORIALS",
                "CREATE_CONTENT", "UPDATE_CONTENT", "CREATE_ASSIGNMENT", "UPDATE_ASSIGNMENT",
                "RESOURCE_READ", "RESOURCE_WRITE", "VIEW_NOTIFICATIONS", "SUPPORT_CREATE"
        ));
        
        // ADMIN - Quản trị viên (tất cả permissions)
        List<String> allPermissions = List.of(
                "VIEW_USERS", "CREATE_USERS", "UPDATE_USERS", "DELETE_USERS",
                "VIEW_ROLES", "CREATE_ROLES", "UPDATE_ROLES", "DELETE_ROLES",
                "MANAGE_TUTORIALS", "PUBLISH_TUTORIALS",
                "CREATE_TUTORIAL", "UPDATE_TUTORIAL", "DELETE_TUTORIAL", "VIEW_OWN_TUTORIALS",
                "CREATE_CONTENT", "UPDATE_CONTENT", "CREATE_ASSIGNMENT", "UPDATE_ASSIGNMENT",
                "VIEW_COURSES", "CREATE_COURSE", "UPDATE_COURSE", "DELETE_COURSE", "ENROLL_COURSE",
                "VIEW_SUBMISSIONS", "EVALUATE_SUBMISSIONS", "SUBMIT_ASSIGNMENT",
                "VIEW_FEEDBACK", "RESPOND_FEEDBACK", "REQUEST_HELP", "VIEW_HELP_REQUESTS",
                "VIEW_NOTIFICATIONS", "MANAGE_NOTIFICATIONS", "VIEW_TEACHER_STATS",
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE",
                "RESOURCE_READ", "RESOURCE_WRITE", "TESTCASE_READ", "TESTCASE_WRITE",
                "SCHEDULE_READ", "NOTIF_READ", "NOTIF_WRITE",
                "USER_MANAGE", "API_MANAGE", "MAINTENANCE_MANAGE", "POLICY_MANAGE"
        );
        R.put("ADMIN", allPermissions);

        for (var e : R.entrySet()) {
            String roleName = e.getKey();
            
            // Check if role already exists with permissions
            Optional<Role> existingRole = roleRepo.findByName(roleName);
            if (existingRole.isPresent() && !existingRole.get().getPermissions().isEmpty()) {
                continue; // Skip if already seeded
            }
            
            Set<Permission> perms = e.getValue().stream()
                    .map(n -> permRepo.findByName(n).orElseThrow()).collect(Collectors.toCollection(LinkedHashSet::new));

            Role role = existingRole.orElseGet(() -> 
                roleRepo.save(Role.builder().name(roleName).description(roleName).build())
            );
            role.setPermissions(perms);
            roleRepo.save(role); // cập nhật roles_permissions(roles_id, permissions_id)
        }
    }

    @Transactional
    protected void seedAdminUser() {
        log.info("Seeding admin user...");
        
        String email = Optional.ofNullable(admin.getEmail()).orElse("admin@apsas.local");
        String name = Optional.ofNullable(admin.getName()).orElse("APSAS Admin");
        String raw = Optional.ofNullable(admin.getPassword()).orElse("Admin@12345");

        if (!userRepo.existsByEmail(email)) {
            try {
                Role adminRole = roleRepo.findByName("ADMIN")
                        .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
                        
                User u = User.builder()
                        .name(name)
                        .email(email)
                        .password(encoder.encode(raw))
                        .status(UserStatus.ACTIVE)
                        .build();
                u.getRoles().add(adminRole);
                userRepo.save(u);
                
                log.info("Created admin user: {}", email);
            } catch (Exception e) {
                log.error("Error creating admin user: {}", e.getMessage());
            }
        } else {
            log.info("Admin user already exists: {}", email);
        }
    }
}
