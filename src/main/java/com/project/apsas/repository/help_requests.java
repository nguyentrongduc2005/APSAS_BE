package com.project.apsas.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface help_requests extends JpaRepository<help_requests, Long> {

}
