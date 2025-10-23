package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 80, unique = true)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;
    @ManyToMany(mappedBy = "roles",fetch = FetchType.LAZY)
    Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Set<Permission> permissions = new HashSet<>();
}
