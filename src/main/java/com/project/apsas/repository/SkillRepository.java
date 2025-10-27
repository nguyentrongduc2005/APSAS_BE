package com.project.apsas.repository;

import com.project.apsas.entity.ProgressSkillId;
import com.project.apsas.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
}
