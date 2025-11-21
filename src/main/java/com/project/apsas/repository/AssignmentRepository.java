package com.project.apsas.repository;

import com.project.apsas.dto.response.assignment.TutorialAssignmentItemDto;
import com.project.apsas.entity.Assignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query("SELECT ca.course.id, COUNT(DISTINCT a.id) " + // Sửa SELECT
            "FROM Assignment a " +
            "JOIN a.courseLinks ca " +
            "WHERE ca.course.id IN :courseIds " +
            "GROUP BY ca.course.id") // Thêm GROUP BY
    List<Object[]> findTotalAssignmentCountsPerCourse(@Param("courseIds") List<Long> courseIds);


    @Query("SELECT ca.course.id, COUNT(DISTINCT a.id) " + // Sửa SELECT
            "FROM Assignment a " +
            "JOIN a.courseLinks ca " +
            "JOIN a.submissions s " +
            "WHERE ca.course.id IN :courseIds " +
            "AND s.userId = :userId " +
            "GROUP BY ca.course.id") // Thêm GROUP BY
    List<Object[]> findSubmittedAssignmentCountsPerCourseByUser( // Sửa kiểu trả về và tên hàm
                                                                 @Param("courseIds") List<Long> courseIds,
                                                                 @Param("userId") Long userId
    );

    @Query("SELECT NEW com.project.apsas.dto.response.assignment.TutorialAssignmentItemDto(" +
            "    a.id, " +
            "    a.title, " +
            "    a.maxScore, " +
            "    s.name, " + // Lấy tên từ skill
            "    a.proficiency, " +
            "    a.attemptsLimit, " +
            "    a.orderNo" +
            ") " +
            "FROM Assignment a " +
            "LEFT JOIN a.skill s " + // LEFT JOIN phòng trường hợp skill là null
            "WHERE a.tutorial.id = :tutorialId")
    List<TutorialAssignmentItemDto> findAssignmentDTOsByTutorialId(@Param("tutorialId") Long tutorialId);

}
