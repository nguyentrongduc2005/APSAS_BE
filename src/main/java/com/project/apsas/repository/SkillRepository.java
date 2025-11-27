package com.project.apsas.repository;

import com.project.apsas.entity.ProgressSkillId;
import com.project.apsas.entity.Skill;
import com.project.apsas.enums.CategorySkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByOrderByNameAsc();
    List<Skill> findByCategoryOrderByNameAsc(CategorySkill category);
}
