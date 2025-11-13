package com.project.apsas.repository;

import com.project.apsas.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.project.apsas.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Transactional
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Hoặc nếu bạn muốn tìm theo userId
    Optional<RefreshToken> findByUserId(Long userId);

    // Xóa refresh token theo userId
    void deleteByUserId(Long userId);
    void deleteByUser(User user);
}
