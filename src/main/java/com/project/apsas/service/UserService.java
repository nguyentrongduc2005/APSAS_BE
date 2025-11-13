package com.project.apsas.service;

import com.project.apsas.dto.student.ProgressDTO;

public interface UserService {
        ProgressDTO getStudentProgress(Long studentId);
}
