package com.project.apsas.repository;

import com.project.apsas.entity.Otp;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

}
