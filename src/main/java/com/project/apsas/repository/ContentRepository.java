package com.project.apsas.repository;

import com.project.apsas.entity.Content;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

}
