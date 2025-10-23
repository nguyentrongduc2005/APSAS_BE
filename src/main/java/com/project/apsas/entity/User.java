package com.project.apsas.entity;

import com.project.apsas.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, length = 120)
    String name;
    @Column(nullable = false, length = 190)
    String email;
    @Column(nullable = false, length = 255)
    String password;
    @Enumerated(EnumType.STRING)
    UserStatus status;
    @Column(name = "created_at", updatable = false, insertable = false)
    LocalDateTime createdAt;

}
