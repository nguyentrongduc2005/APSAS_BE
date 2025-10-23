package com.project.apsas.entity;

import com.project.apsas.enums.ContentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "contents")
public class contents {
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

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "tutorial_id", insertable = false, updatable = false)
    // private Tutorial tutorial;  

}
