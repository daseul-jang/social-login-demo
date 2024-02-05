package com.techit.sociallogindemo.security.authentiation;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techit.sociallogindemo.entity.Member;
import com.techit.sociallogindemo.exception.UserNotFoundException;
import com.techit.sociallogindemo.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new UserNotFoundException("존재하지 않는 계정입니다"));
		return UserPrincipal.create(member);
	}
}
