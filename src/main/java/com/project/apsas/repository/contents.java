package com.project.apsas.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface contents extends JpaRepository<contents, Long> {

}
