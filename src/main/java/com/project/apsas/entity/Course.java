package com.project.apsas.entity;

import com.project.apsas.enums.CourseVisibility;

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
@Table(name = "courses")
public class Course {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 160, nullable = false)
    private String name;

    @Column(length = 60)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CourseVisibility visibility;

    @Column(length = 50)
    private String type;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "limit")
    private Integer limit;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<CourseAssignment> assignmentLinks = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<CourseContent> contentLinks = new HashSet<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private Set<HelpRequest> helpRequests = new HashSet<>();
}

