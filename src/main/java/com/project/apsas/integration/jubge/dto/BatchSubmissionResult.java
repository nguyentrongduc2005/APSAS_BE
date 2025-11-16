package com.project.apsas.integration.jubge.dto;

import java.util.List;

public class BatchSubmissionResult {
    private List<SubmissionRCEResponse> submissions;

    // Getter
    public List<SubmissionRCEResponse> getSubmissions() {
        return submissions;
    }

    // Setter
    public void setSubmissions(List<SubmissionRCEResponse> submissions) {
        this.submissions = submissions;
    }
}
