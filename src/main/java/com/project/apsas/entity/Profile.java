package com.project.apsas.entity;

import com.project.apsas.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(length = 512)
    String avatarUrl;

    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @Column(length = 30)
    String phone;

    @Column(length = 255)
    String address;

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    User user;


}
