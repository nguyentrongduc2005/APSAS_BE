package com.project.apsas.entity;

import com.project.apsas.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 30)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    

}
