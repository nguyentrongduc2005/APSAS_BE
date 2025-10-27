package com.project.apsas.repository;

import com.project.apsas.entity.HelpRequest;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface HelpRequestsRepository extends JpaRepository<HelpRequest, Long> {

}
