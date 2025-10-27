package com.project.apsas.entity;

import com.project.apsas.enums.StatusTutorial;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tutorials")
public class Tutorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    User createdBy;

    @Column(length = 200)
    String title;

    @Column(columnDefinition = "TEXT")
    String summary;

    @Enumerated(EnumType.STRING)
    StatusTutorial status;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;
}
