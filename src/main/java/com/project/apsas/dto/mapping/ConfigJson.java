package com.project.apsas.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class ConfigJson {
    @JsonProperty("testCase")
    private List<TestCase> testCases;
}
