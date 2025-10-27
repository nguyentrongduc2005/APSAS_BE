package com.project.apsas.repository;

import com.project.apsas.entity.ProgressSkill;
import com.project.apsas.entity.ProgressSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgressSkillRepository extends JpaRepository<ProgressSkill, ProgressSkillId> {
}
