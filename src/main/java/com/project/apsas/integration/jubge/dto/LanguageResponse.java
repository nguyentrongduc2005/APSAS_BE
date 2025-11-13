package com.project.apsas.integration.jubge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageResponse {
    private int id;
    private String name;

    @JsonProperty("is_archived")
    private boolean isArchived;
}
