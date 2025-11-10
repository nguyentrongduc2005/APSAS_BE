package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "verification_codes", indexes = {
        @Index(name = "idx_verification_email", columnList = "email")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationCode {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String email;

    @Column(nullable = false, length = 12)
    String code; // 6-digit

    @Column(nullable = false)
    Instant expiresAt;

    @Column(nullable = false)
    boolean used;

    @Column(nullable = false)
    Instant createdAt;
}
