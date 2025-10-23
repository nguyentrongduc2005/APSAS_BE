package com.project.apsas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "assignment_evaluation_maps")

public class assignment_evaluation_maps {

    @Id
    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Id
    @Column(name = "evaluation_id", nullable = false)
    private Long evaluationId;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", insertable = false, updatable = false)
    private assignments assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", insertable = false, updatable = false)
    private assignment_evaluation_maps evaluation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long assignmentId;
        private Long evaluationId;
    }
}
