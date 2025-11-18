package com.project.apsas.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.apsas.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "media")
public class Media {
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
    @Column(name = "public_id", length = 255)
    private String pubicId;
    @Column(length = 255)
    private String caption;

    @Column(name = "order_no")
    private Integer orderNo;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "content_id", insertable = false, updatable = false)
    private Content content;
}

