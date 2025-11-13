package com.project.apsas.repository;

import com.project.apsas.entity.HelpRequest;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface HelpRequestsRepository extends JpaRepository<HelpRequest, Long> {

	// Find help requests by course id with pagination
	org.springframework.data.domain.Page<HelpRequest> findByCourseId(Long courseId, org.springframework.data.domain.Pageable pageable);

}
