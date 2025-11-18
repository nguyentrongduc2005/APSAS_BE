package com.project.apsas.dto.request.admin;

import com.project.apsas.enums.ContentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewContentRequest {
    @NotNull(message = "STATUS_REQUIRED")
    private ContentStatus status; // PUBLISHED or REJECTED
    
    private String reviewNote; // Optional note from admin
}
