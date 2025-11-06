package com.project.apsas.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

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
@Transactional
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

  @Override public void run(ApplicationArguments args) {
    if (!seedEnabled) return;

    // 1) permissions (map theo sidebar)
    List<String> P = List.of(
      "DASHBOARD_VIEW","PROFILE_READ","PROFILE_WRITE","SUPPORT_CREATE",
      "COURSE_READ","COURSE_CREATE","COURSE_UPDATE","COURSE_DELETE",
      "ASSIGNMENT_READ","ASSIGNMENT_CREATE","ASSIGNMENT_UPDATE","ASSIGNMENT_DELETE",
      "TESTCASE_READ","TESTCASE_WRITE","RESOURCE_READ","RESOURCE_WRITE",
      "SCHEDULE_READ","NOTIF_READ","NOTIF_WRITE",
      "USER_MANAGE","API_MANAGE","MAINTENANCE_MANAGE","POLICY_MANAGE"
    );
    for (String name : P)
      permRepo.findByName(name).orElseGet(() ->
        permRepo.save(Permission.builder().name(name)
          .description(name.replace('_',' ').toLowerCase()).build()));

    // 2) roles → permissions
    Map<String, List<String>> R = new LinkedHashMap<>();
    R.put("GUEST", List.of("COURSE_READ","RESOURCE_READ","SUPPORT_CREATE"));
    R.put("STUDENT", List.of("DASHBOARD_VIEW","COURSE_READ","ASSIGNMENT_READ","RESOURCE_READ",
                             "SCHEDULE_READ","NOTIF_READ","PROFILE_READ","PROFILE_WRITE","SUPPORT_CREATE"));
    R.put("LECTURER", List.of("DASHBOARD_VIEW","COURSE_READ","COURSE_CREATE","COURSE_UPDATE",
                              "ASSIGNMENT_READ","ASSIGNMENT_CREATE","ASSIGNMENT_UPDATE",
                              "TESTCASE_READ","TESTCASE_WRITE","RESOURCE_READ","RESOURCE_WRITE",
                              "NOTIF_READ","NOTIF_WRITE","PROFILE_READ","PROFILE_WRITE","SUPPORT_CREATE"));
    R.put("CONTENT_PROVIDER", List.of("DASHBOARD_VIEW","COURSE_READ","COURSE_CREATE","COURSE_UPDATE",
                                      "ASSIGNMENT_READ","ASSIGNMENT_CREATE","ASSIGNMENT_UPDATE",
                                      "TESTCASE_READ","TESTCASE_WRITE","RESOURCE_READ","RESOURCE_WRITE",
                                      "PROFILE_READ","PROFILE_WRITE","SUPPORT_CREATE"));
    R.put("ADMIN", new ArrayList<>(P));

    for (var e : R.entrySet()) {
      String roleName = e.getKey();
      Set<Permission> perms = e.getValue().stream()
        .map(n -> permRepo.findByName(n).orElseThrow()).collect(Collectors.toCollection(LinkedHashSet::new));

      Role role = roleRepo.findByName(roleName)
        .orElseGet(() -> roleRepo.save(Role.builder().name(roleName).description(roleName).build()));
      role.setPermissions(perms);
      roleRepo.save(role); // cập nhật roles_permissions(roles_id, permissions_id)
    }

    // 3) admin mặc định (users, users_roles)
    String email = Optional.ofNullable(admin.getEmail()).orElse("admin@apsas.local");
    String name  = Optional.ofNullable(admin.getName()).orElse("APSAS Admin");
    String raw   = Optional.ofNullable(admin.getPassword()).orElse("Admin@12345");

    if (!userRepo.existsByEmail(email)) {
      Role adminRole = roleRepo.findByName("ADMIN").orElseThrow();
      User u = User.builder()
        .name(name).email(email).password(encoder.encode(raw)).status(UserStatus.ACTIVE).build();
      u.getRoles().add(adminRole);           // users_roles(users_id, roles_id)
      userRepo.save(u);
    }
  }
}
