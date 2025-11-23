package com.project.apsas.entity;

import com.project.apsas.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;
    @Column(nullable = false, length = 120)
    String name;
    @Column(nullable = false, length = 190,  unique = true)
    @EqualsAndHashCode.Include
    String email;
    @Column(nullable = false, length = 255)
    String password;
    @Enumerated(EnumType.STRING)
    UserStatus status;
    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @lombok.Builder.Default
    @ToString.Exclude
    Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Otp otp;
    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    Set<Course> courses = new HashSet<>();
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude
    @lombok.Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private Set<HelpRequest> helpRequests = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @lombok.Builder.Default
    @ToString.Exclude
    private Set<Notification> notifications = new HashSet<>();
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Progress progress;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    RefreshToken refreshToken;

    public String getAvatarUrl() {

        return "";
    }
}
