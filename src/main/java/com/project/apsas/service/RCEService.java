package com.project.apsas.service;

import com.project.apsas.dto.mapping.ReportCongfigSubmission;
import com.project.apsas.dto.request.SubmissionRequestDto;

public interface RCEService {
    ReportCongfigSubmission evaluateCode(SubmissionRequestDto request) throws Exception;
}
