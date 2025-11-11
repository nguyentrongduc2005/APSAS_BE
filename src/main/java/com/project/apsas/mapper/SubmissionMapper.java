package com.project.apsas.mapper;

import com.project.apsas.dto.response.CodeFeedbackDTO;
import com.project.apsas.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubmissionMapper {
    void updateSubmissionFromDto(CodeFeedbackDTO dto, @MappingTarget Submission entity);
}
