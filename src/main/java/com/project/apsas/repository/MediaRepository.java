package com.project.apsas.repository;

import com.project.apsas.entity.Media;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    Set<Media> findByContentId(Long contentId);
}
