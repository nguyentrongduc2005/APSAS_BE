package com.project.apsas.repository;

import com.project.apsas.dto.response.content.TutorialContentItemDto;
import com.project.apsas.entity.Content;
import com.project.apsas.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, JpaSpecificationExecutor<Content> {
    
    // Count methods for statistics
    long countByStatus(ContentStatus status);

    @Query("SELECT NEW com.project.apsas.dto.response.content.TutorialContentItemDto(" +
            "    c.id, " +
            "    c.title, " +
            "    SUM(CASE WHEN m.type = 'IMAGE' THEN 1 ELSE 0 END), " +
            "    SUM(CASE WHEN m.type = 'VIDEO' THEN 1 ELSE 0 END), " +
            "    c.orderNo" +
            ") " +
            "FROM Content c " +
            "LEFT JOIN c.mediaList m " + // LEFT JOIN phòng trường hợp content không có media
            "WHERE c.tutorial.id = :tutorialId " +
            "GROUP BY c.id, c.title, c.orderNo") // Phải GROUP BY tất cả các trường không tổng hợp
    List<TutorialContentItemDto> findContentDTOsByTutorialId(@Param("tutorialId") Long tutorialId);
}
