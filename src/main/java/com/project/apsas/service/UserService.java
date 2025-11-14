package com.project.apsas.service;

import com.project.apsas.dto.student.DailyScoreDTO;
import com.project.apsas.dto.student.ProgressDTO;

import java.time.LocalDate;
import java.util.List;

public interface UserService {

    ProgressDTO getStudentCurrentProgress(Long studentId);
    List<DailyScoreDTO> getStudentDailyScores(Long studentId, LocalDate from, LocalDate to);
}
