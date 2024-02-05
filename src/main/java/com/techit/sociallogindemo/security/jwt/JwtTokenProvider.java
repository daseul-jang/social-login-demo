package com.techit.sociallogindemo.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.techit.sociallogindemo.security.authentiation.UserDetailsServiceImpl;
import com.techit.sociallogindemo.security.authentiation.UserPrincipal;
import com.techit.sociallogindemo.security.jwt.dto.JwtResponse;
import com.techit.sociallogindemo.security.jwt.entity.JwtRefreshToken;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	@Value("${jwt.secret-key}")
	private String secretKey;

	@Value("${jwt.access-token-expiration-msec}")
	private long accessTokenExpirationMsec;

	@Value("${jwt.refresh-token-expiration-msec}")
	private long refreshTokenExpirationMsec;

	private final UserDetailsServiceImpl userDetailsService;

	public SecretKey createKey(String tokenSecret) {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenSecret));
	}

	public JwtResponse createAccessToken(Authentication authentication) {
		String token = createToken(authentication, accessTokenExpirationMsec);
		Date expiryDate = createExpiryDate(accessTokenExpirationMsec);

		log.info("token provider exp: {}", expiryDate.getTime());

		return JwtResponse.builder()
			.accessToken(token)
			.accessTokenExp(expiryDate.getTime())
			.build();
	}

	public JwtRefreshToken createRefreshToken(Authentication authentication) {
		log.info("리프레시 토큰 생성하는 곳");
		String token = createToken(authentication, refreshTokenExpirationMsec);
		return JwtRefreshToken.builder()
			.refreshToken(token)
			.expiryDate(createExpiryDate(refreshTokenExpirationMsec).toInstant())
			.build();
	}

	public String createToken(Authentication authentication, long tokenExpiryDate) {
		return Jwts.builder()
			.claim("email", authentication.getName())
			.subject(authentication.getName())
			.issuedAt(new Date())
			.expiration(createExpiryDate(tokenExpiryDate))
			.signWith(createKey(secretKey))
			.compact();
	}

	public Date createExpiryDate(long tokenExpiryDate) {
		return new Date(new Date().getTime() + tokenExpiryDate);
	}

	// 토큰에서 사용자 이름(username) 추출
	public String getUsernameFromToken(String token) {
		return Jwts.parser()
			.verifyWith(createKey(secretKey))
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getSubject();
	}

	// 토큰에서 사용자 이름을 추출한 후 해당 사용자 조회
	public Authentication getAuthentication(String token) {
		UserPrincipal userPrincipal = (UserPrincipal)userDetailsService.loadUserByUsername(getUsernameFromToken(token));
		return new UsernamePasswordAuthenticationToken(userPrincipal, "", userPrincipal.getAuthorities());
	}

	public boolean validateToken(String token) {
		log.info("validateToken");
		log.info(token);
		try {
			Jwts.parser().verifyWith(createKey(secretKey)).build().parseSignedClaims(token);
			return true;
		} catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException |
				 IllegalArgumentException e) {
			log.error("Token validation failed", e);
			return false;
		}
	}
}
