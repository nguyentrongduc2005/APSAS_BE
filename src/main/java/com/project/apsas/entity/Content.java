package com.project.apsas.entity;

import com.project.apsas.enums.ContentStatus;
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
@Table(name = "contents")
public class Content {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutorial_id", nullable = false)
    private Long tutorialId;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "body_md", columnDefinition = "MEDIUMTEXT")
    private String bodyMd;

    @Column(name = "body_html_cached", columnDefinition = "MEDIUMTEXT")
    private String bodyHtmlCached;

    @Column(name = "order_no")
    private Integer orderNo;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ContentStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutorial_id", insertable = false, updatable = false)
    private Tutorial tutorial;  

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CourseContent> courseLinks = new HashSet<>();

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Media> mediaList = new HashSet<>();

}
