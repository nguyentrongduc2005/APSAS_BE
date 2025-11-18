package com.project.apsas.repository;

import com.project.apsas.entity.CourseContent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, CourseContent.PK> {
    @Query("SELECT cc.course.id, COUNT(cc) FROM CourseContent cc WHERE cc.course.id IN :courseIds GROUP BY cc.course.id")
    List<Object[]> findTotalLessonsByCourseIds(@Param("courseIds") List<Long> courseIds);

    // --- 2. Đếm số bài học PUBLIC ---
    // Giả sử Entity Content có trường 'visibility'
    @Query("SELECT cc.course.id, COUNT(cc) FROM CourseContent cc WHERE cc.course.id IN :courseIds GROUP BY cc.course.id")
    List<Object[]> findPublicLessonsByCourseIds(@Param("courseIds") List<Long> courseIds);

    @Query("SELECT COUNT(cc) FROM CourseContent cc WHERE cc.course.id = :courseId")
    int countByCourseId(@Param("courseId") Long courseId);


    @Query("SELECT COUNT(DISTINCT cc.contentId) FROM CourseContent cc WHERE cc.courseId IN :courseIds")
    int countContentsByCourseIds(@Param("courseIds") List<Long> courseIds);
}