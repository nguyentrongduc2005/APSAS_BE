package com.project.apsas.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface courses_contents extends JpaRepository<courses_contents, Long> {

}
