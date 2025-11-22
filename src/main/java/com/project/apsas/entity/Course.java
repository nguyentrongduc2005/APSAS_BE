package com.project.apsas.entity;

import com.project.apsas.enums.CourseVisibility;

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
@EqualsAndHashCode(of = {"id"})
@Table(name = "courses")
public class Course {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 160, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 60)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CourseVisibility visibility;

    @Column(length = 50)
    private String type;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "`limit`")
    private Integer limit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CourseAssignment> assignmentLinks = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User creator;
    
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CourseContent> contentLinks = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<HelpRequest> helpRequests = new HashSet<>();
}

