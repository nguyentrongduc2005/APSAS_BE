package com.project.apsas.configuration;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@Profile("db")
@RequiredArgsConstructor
public class DataSeeder {

    // Ví dụ: private final PermissionRepository permissionRepository;
    // private final RoleRepository roleRepository;
    // private final UserRepository userRepository;

    @PostConstruct
    public void seed() {
        // TODO: logic seed DB khi chạy profile "db"
    }
}
