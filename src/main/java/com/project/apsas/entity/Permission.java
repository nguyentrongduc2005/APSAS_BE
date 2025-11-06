package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;
    @Column(nullable = false, unique = true, length = 120)
    @EqualsAndHashCode.Include
    String name;
    @Column(columnDefinition = "TEXT")
    String description;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @lombok.Builder.Default
    Set<Role> roles = new HashSet<>();
}
