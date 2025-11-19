package com.project.apsas.dto.response.course;

import com.project.apsas.enums.ContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResourceListDTO {
    private List<ContentResourceDTO> availableContents;
    private List<AssignmentResourceDTO> availableAssignments;

}
