package com.project.apsas.repository;

import com.project.apsas.entity.Tutorial;
import com.project.apsas.enums.TutorialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial,Long>, JpaSpecificationExecutor<Tutorial> {
    // Lấy tất cả tutorial do một user tạo
    List<Tutorial> findByCreatedBy(Long createdBy);
    
    // Count by status for statistics
    long countByStatus(TutorialStatus status);
}
