package com.techit.sociallogindemo.security.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		try {
			String accessToken = getBearerToken(request);

			AbstractAuthenticationToken authentication = new AnonymousAuthenticationToken(
				"anonymous", Optional.empty(), Collections.singletonList(new SimpleGrantedAuthority("anonymous"))
			);

			if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
				authentication = (AbstractAuthenticationToken)jwtTokenProvider.getAuthentication(accessToken);
			}

			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			log.error("{}: {}", "토큰 인증 실패", e.getMessage());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
			return;
		}

		filterChain.doFilter(request, response);
	}

	// 요청 header에서 token 추출
	public String getBearerToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}
}
