package com.techit.sociallogindemo.security.jwt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techit.sociallogindemo.security.jwt.entity.JwtRefreshToken;

public interface JwtRefreshTokenRepository extends JpaRepository<JwtRefreshToken, Long> {
	Optional<JwtRefreshToken> findByMember_Id(Long id);
}
