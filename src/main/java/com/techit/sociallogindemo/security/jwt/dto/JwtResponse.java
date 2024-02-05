package com.techit.sociallogindemo.security.jwt.dto;

import com.techit.sociallogindemo.dto.MemberResponse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@AllArgsConstructor
public class JwtResponse {
	private final String accessToken;
	private final Long accessTokenExp;
	private final String refreshToken;
	private MemberResponse member;
}
