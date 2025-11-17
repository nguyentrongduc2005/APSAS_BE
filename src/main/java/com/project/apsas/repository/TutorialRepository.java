package com.project.apsas.repository;

import com.project.apsas.entity.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial,Long> {
    // Lấy tất cả tutorial do một user tạo
    List<Tutorial> findByCreatedBy(Long createdBy);
}
