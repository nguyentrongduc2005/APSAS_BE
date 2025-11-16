package com.project.apsas.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigJson {
    @JsonProperty("testCase")
    private List<TestCase> testCases;
}
