package com.project.apsas.repository;

import com.project.apsas.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.apsas.entity.Course;
import com.project.apsas.enums.CourseVisibility;
import com.project.apsas.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface CourseRepository extends JpaRepository<Course, Long> {



    Page<Course> findByNameContainingIgnoreCaseAndVisibility(
            String search,
            CourseVisibility visibility, // Tham số Enum lọc
            Pageable pageable      // Tham số phân trang và sắp xếp
    );

    Page<Course> findByNameContainingIgnoreCaseAndCreator(
            String search,
            User creator,
            Pageable pageable
    );
    @Query("SELECT c FROM Course c JOIN c.enrollments e " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND e.user = :student") // <-- e.user là User Entity trong Enrollment
    Page<Course> findByNameContainingIgnoreCaseAndEnrollmentsContains(
            @Param("search") String search,
            @Param("student") User student, // <-- Tham số là User (Khớp với Service)
            Pageable pageable
    );
    Page<Course> findByCreator(User creator, Pageable pageable);
    @Query("SELECT c FROM Course c JOIN c.enrollments e " +
            "WHERE e.user = :student")
    Page<Course> findByEnrollmentsContains(
            @Param("student") User student,
            Pageable pageable
    );

    Page<Course> findByVisibility(CourseVisibility visibility, Pageable pageable);
    @Query("SELECT COUNT(c) FROM Course c WHERE c.creator.id = :creatorId")
    Long countCoursesByCreatorId(@Param("creatorId") Long creatorId);

    @Query("""
        select c from Course c
        where
          (
            (:visibility is null and c.visibility = com.project.apsas.enums.CourseVisibility.PUBLIC)
            or (:visibility is not null and c.visibility = :visibility)
          )
          and (:keyword is null
               or lower(c.name) like lower(concat('%', :keyword, '%'))
               or lower(c.code) like lower(concat('%', :keyword, '%')))
        """)
    Page<Course> searchPublic(@Param("keyword") String keyword,
                              @Param("visibility") CourseVisibility visibility,
                              Pageable pageable);

//    @Query("""
//        select new com.project.apsas.dto.teacher.TeacherCourseSummaryResponse(
//            c.id, c.code, c.name, c.visibility,
//            (select count(a) from CourseAssignment a where a.course.id = c.id),
//            (select avg(s.score) from Submission s where s.course.id = c.id)
//        )
//        from Course c
//        where (:teacherId is null or c.teacher.id = :teacherId)
//        """)
//    java.util.List<com.project.apsas.dto.teacher.TeacherCourseSummaryResponse>
//    findSummariesByTeacherId(@Param("teacherId") Long teacherId);
    boolean existsByCode(String code);


    Optional<Course> findByCode(String code);


}
