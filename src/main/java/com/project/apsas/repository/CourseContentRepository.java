package com.project.apsas.repository;

import com.project.apsas.entity.CourseContent;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {

}
