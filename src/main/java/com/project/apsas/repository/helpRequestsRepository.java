package com.project.apsas.repository;

import com.project.apsas.entity.help_requests;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface helpRequestsRepository extends JpaRepository<help_requests, Long> {

}
