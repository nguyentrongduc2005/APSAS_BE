package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;

    @Column(nullable = false, length = 80, unique = true)
    @EqualsAndHashCode.Include
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;
    @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY)
    @lombok.Builder.Default
    Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @lombok.Builder.Default
    Set<Permission> permissions = new HashSet<>();
}
