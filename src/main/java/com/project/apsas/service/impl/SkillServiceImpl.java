package com.project.apsas.service.impl;

import com.project.apsas.dto.SkillDTO;
import com.project.apsas.entity.Skill;
import com.project.apsas.enums.CategorySkill;
import com.project.apsas.repository.SkillRepository;
import com.project.apsas.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SkillDTO> getAllSkills() {
        log.info("Getting all skills");
        List<Skill> skills = skillRepository.findAllByOrderByNameAsc();
        return skills.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillDTO> getSkillsByCategory(String category) {
        log.info("Getting skills by category: {}", category);
        CategorySkill categoryEnum = CategorySkill.valueOf(category.toUpperCase());
        List<Skill> skills = skillRepository.findByCategoryOrderByNameAsc(categoryEnum);
        return skills.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private SkillDTO convertToDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .category(skill.getCategory())
                .build();
    }
}