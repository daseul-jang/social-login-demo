package com.techit.sociallogindemo.security.authentiation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.techit.sociallogindemo.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@AllArgsConstructor
public class UserPrincipal implements OAuth2User, UserDetails {
	private Member member;
	private Collection<? extends GrantedAuthority> authorities;
	private Map<String, Object> attributes;

	public static UserPrincipal create(Member member) {
		List<GrantedAuthority> authorities = Collections.
			singletonList(
				new SimpleGrantedAuthority(
					"admin".equals(member.getEmail()) ?
						"admin" : "user"
				)
			);

		return UserPrincipal.builder()
			.member(member)
			.authorities(authorities)
			.build();
	}

	public static UserPrincipal create(Member member, Map<String, Object> attributes) {
		log.info("UserPrincipal create attributes: {}", attributes);
		UserPrincipal userPrincipal = UserPrincipal.create(member);
		userPrincipal.setAttributes(attributes);
		return userPrincipal;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getUsername() {
		return member.getEmail();
	}

	@Override
	public String getName() {
		return member.getName();
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
}
