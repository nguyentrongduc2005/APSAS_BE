package com.project.apsas.service;

import com.project.apsas.dto.SkillDTO;
import java.util.List;

public interface SkillService {
    List<SkillDTO> getAllSkills();
    List<SkillDTO> getSkillsByCategory(String category);
}