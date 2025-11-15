package com.project.apsas.mapper;

import com.project.apsas.dto.mapping.TestCase;
import com.project.apsas.dto.response.assignment.CreateAssignmentResponse;
import com.project.apsas.dto.response.assignment.TestCaseConfig;
import com.project.apsas.entity.Assignment;

import com.project.apsas.service.impl.MarkdownService;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses =  {MarkdownService.class}
)
public interface AssignmentMapper {

    @Mappings({
            @Mapping(source = "statementMd",
                    target = "statementHtml",
                    qualifiedByName = "markdownToHtml"),
            @Mapping(source = "maxScore",
                    target = "maxScore",
                    qualifiedByName = "bigDecimalToInt")
    })
    CreateAssignmentResponse toCreateResponse(Assignment assignment);

    @Named("bigDecimalToInt")
    default int bigDecimalToInt(BigDecimal score) {
        // Sửa 'new' thành 'null'
        return (score == null) ? 0 : score.intValue();
    }
    List<TestCaseConfig> toTestCaseConfigs(List<TestCase> testCases);
}
