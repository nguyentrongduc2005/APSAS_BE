package com.project.apsas.dto.request.assignment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentListItemDTO {
    private Long id;
    private String title;
    private LocalDateTime dueAt;




}
