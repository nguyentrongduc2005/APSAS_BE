package com.project.apsas.entity;

import com.project.apsas.enums.MediaType;
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
@Table(name = "media")
public class media {
 @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaType type;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(length = 255)
    private String caption;

    @Column(name = "order_no")
    private Integer orderNo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private contents content;
}

