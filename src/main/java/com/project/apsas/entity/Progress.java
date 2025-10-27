package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "progress")
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int totalAttemptNo;

    float acceptance;

    @Column( updatable = false, insertable = false)
    LocalDateTime createdAt;
    @OneToOne(fetch = FetchType.LAZY)
    User user;

    @OneToMany(mappedBy = "progress", cascade = CascadeType.ALL)
    Set<ProgressSkill> progressSkills;
}
