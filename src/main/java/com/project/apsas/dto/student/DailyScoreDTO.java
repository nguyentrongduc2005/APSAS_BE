package com.project.apsas.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyScoreDTO {
    private LocalDate date;
    private Double  score;
    public DailyScoreDTO(java.sql.Date sqlDate, Double avgScore) {
        this.date = (sqlDate != null) ? sqlDate.toLocalDate() : null;
        this.score = avgScore;
    }

}
