package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(cascade = CascadeType.ALL)
    User user;

    @Column(length = 100)
    String name;
    @Column(unique = true, nullable = false, length = 255)
    String tokenHash;

    LocalDateTime expiresAt;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;
}
