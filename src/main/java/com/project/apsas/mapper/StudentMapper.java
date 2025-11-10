package com.project.apsas.mapper;

import com.project.apsas.dto.student.StudentRequest;
import com.project.apsas.dto.student.StudentResponse;
import com.project.apsas.entity.Student;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE 
)
public interface StudentMapper {

    Student toEntity(StudentRequest req);

    StudentResponse toResponse(Student entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Student target, StudentRequest src);
}
