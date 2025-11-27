package com.project.apsas.controller;

import com.project.apsas.dto.ApiResponse;
import com.project.apsas.dto.SkillDTO;
import com.project.apsas.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("skills")
@RequiredArgsConstructor
@Slf4j
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ApiResponse<List<SkillDTO>> getAllSkills() {
        log.info("REST request to get all skills");
        List<SkillDTO> skills = skillService.getAllSkills();

        return ApiResponse.<List<SkillDTO>>builder()
                .code("OK") // Hoặc bạn có thể dùng "200" tùy quy ước
                .message("Lấy danh sách skills thành công")
                .data(skills)
                .build();
    }

    @GetMapping("/category/{category}")
    public ApiResponse<List<SkillDTO>> getSkillsByCategory(
            @PathVariable String category) {
        log.info("REST request to get skills by category: {}", category);
        List<SkillDTO> skills = skillService.getSkillsByCategory(category);

        return ApiResponse.<List<SkillDTO>>builder()
                .code("OK")
                .message("Lấy danh sách skills theo category thành công")
                .data(skills)
                .build();
    }
}