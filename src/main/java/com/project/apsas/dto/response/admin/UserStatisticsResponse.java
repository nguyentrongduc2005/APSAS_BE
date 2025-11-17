package com.project.apsas.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Long totalUsers;
    private Long activeUsers;
    private Long blockedUsers;
    private Long unverifiedUsers;
    private Long studentsCount;
    private Long lecturersCount;
    private Long adminsCount;
}
