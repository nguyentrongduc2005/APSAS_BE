package com.project.apsas.dto.request.assignment;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class SetTimeRequest {



    // Sửa lỗi: Thêm @DateTimeFormat để Spring hiểu "yyyy-MM-dd HH:mm:ss"
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openAt;


    // Sửa lỗi: Thêm @DateTimeFormat để Spring hiểu "yyyy-MM-dd HH:mm:ss"
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueAt;


}
