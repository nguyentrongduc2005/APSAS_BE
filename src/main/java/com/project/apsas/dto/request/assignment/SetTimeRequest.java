package com.project.apsas.dto.request.assignment;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SetTimeRequest {

    LocalDateTime openAt;

    LocalDateTime dueAt;

}
