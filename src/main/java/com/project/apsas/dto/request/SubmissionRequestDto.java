package com.project.apsas.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequestDto {
    private String configJson; // Chuỗi JSON chứa { "testCase": [...] }
    private String code;       // Code của người dùng
    private int languageId;    // ID ngôn ngữ của Judge0 (vd: 71)
}
