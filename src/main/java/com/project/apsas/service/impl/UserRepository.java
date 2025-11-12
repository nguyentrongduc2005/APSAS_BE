package com.project.apsas.service.impl;

import com.project.apsas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

interface UserRepository extends JpaRepository<User, Long> {

}
