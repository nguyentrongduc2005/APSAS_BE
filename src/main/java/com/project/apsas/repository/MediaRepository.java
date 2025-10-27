package com.project.apsas.repository;

import com.project.apsas.entity.Media;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
}
