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
    public void run(ApplicationArguments args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled");
            return;
        }

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
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE",
                "COURSE_READ", "COURSE_CREATE", "COURSE_UPDATE", "COURSE_DELETE",
                "ASSIGNMENT_READ", "ASSIGNMENT_CREATE", "ASSIGNMENT_UPDATE", "ASSIGNMENT_DELETE",
                "TESTCASE_READ", "TESTCASE_WRITE", "RESOURCE_READ", "RESOURCE_WRITE",
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
        R.put("GUEST", List.of("COURSE_READ", "RESOURCE_READ", "SUPPORT_CREATE"));
        R.put("STUDENT", List.of("DASHBOARD_VIEW", "COURSE_READ", "ASSIGNMENT_READ", "RESOURCE_READ",
                "SCHEDULE_READ", "NOTIF_READ", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE"));
        R.put("LECTURER", List.of("DASHBOARD_VIEW", "COURSE_READ", "COURSE_CREATE", "COURSE_UPDATE",
                "ASSIGNMENT_READ", "ASSIGNMENT_CREATE", "ASSIGNMENT_UPDATE",
                "TESTCASE_READ", "TESTCASE_WRITE", "RESOURCE_READ", "RESOURCE_WRITE",
                "NOTIF_READ", "NOTIF_WRITE", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE"));
        R.put("CONTENT_PROVIDER", List.of("DASHBOARD_VIEW", "COURSE_READ", "COURSE_CREATE", "COURSE_UPDATE",
                "ASSIGNMENT_READ", "ASSIGNMENT_CREATE", "ASSIGNMENT_UPDATE",
                "TESTCASE_READ", "TESTCASE_WRITE", "RESOURCE_READ", "RESOURCE_WRITE",
                "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE"));
        
        // ADMIN gets all permissions
        List<String> allPermissions = List.of(
                "DASHBOARD_VIEW", "PROFILE_READ", "PROFILE_WRITE", "SUPPORT_CREATE",
                "COURSE_READ", "COURSE_CREATE", "COURSE_UPDATE", "COURSE_DELETE",
                "ASSIGNMENT_READ", "ASSIGNMENT_CREATE", "ASSIGNMENT_UPDATE", "ASSIGNMENT_DELETE",
                "TESTCASE_READ", "TESTCASE_WRITE", "RESOURCE_READ", "RESOURCE_WRITE",
                "SCHEDULE_READ", "NOTIF_READ", "NOTIF_WRITE",
                "USER_MANAGE", "API_MANAGE", "MAINTENANCE_MANAGE", "POLICY_MANAGE"
        );
        R.put("ADMIN", allPermissions);

        for (var entry : R.entrySet()) {
            String roleName = entry.getKey();
            try {
                Set<Permission> perms = entry.getValue().stream()
                        .map(n -> permRepo.findByName(n).orElseThrow(
                                () -> new RuntimeException("Permission not found: " + n)))
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                Optional<Role> existingRole = roleRepo.findByName(roleName);
                Role role;
                
                if (existingRole.isPresent()) {
                    role = existingRole.get();
                    log.debug("Role {} already exists, updating permissions", roleName);
                } else {
                    role = Role.builder()
                            .name(roleName)
                            .description(roleName)
                            .build();
                    role = roleRepo.save(role);
                    log.debug("Created new role: {}", roleName);
                }
                
                // Update permissions
                role.setPermissions(perms);
                roleRepo.save(role);
                
            } catch (Exception e) {
                log.error("Error seeding role {}: {}", roleName, e.getMessage());
            }
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
