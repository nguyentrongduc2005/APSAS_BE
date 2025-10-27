package com.project.apsas.entity;

import com.project.apsas.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, length = 120)
    String name;
    @Column(nullable = false, length = 190,  unique = true)
    String email;
    @Column(nullable = false, length = 255)
    String password;
    @Enumerated(EnumType.STRING)
    UserStatus status;
    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Profile profile;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Progress progress;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    Set<Tutorial> tutorials = new HashSet<>();

//    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    Set<UserNotification> userNotifications = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Set<Submission> submissions = new HashSet<>();
}
