package com.techit.sociallogindemo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techit.sociallogindemo.entity.Member;

import reactor.core.publisher.Mono;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);
}
