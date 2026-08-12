package com.plantify.repository;

import com.plantify.entity.JwtToken;
import com.plantify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);
    boolean existsByToken(String token);
    List<JwtToken> findByUser(User user);
    List<JwtToken> findByUserUserId(Long userId);
    Optional<JwtToken> findFirstByUserUserId(Long userId);
    void deleteByUser(User user);
    void deleteByUserUserId(Long userId);
    void deleteByToken(String token);
}
