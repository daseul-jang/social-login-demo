package com.techit.sociallogindemo.service;

import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.techit.sociallogindemo.dto.MemberResponse;
import com.techit.sociallogindemo.dto.OAuth2UserInfo;
import com.techit.sociallogindemo.entity.Member;
import com.techit.sociallogindemo.enums.ProviderType;
import com.techit.sociallogindemo.exception.OAuth2AuthenticationProcessingException;
import com.techit.sociallogindemo.exception.UserNotFoundException;
import com.techit.sociallogindemo.repository.MemberRepository;
import com.techit.sociallogindemo.security.authentiation.UserPrincipal;
import com.techit.sociallogindemo.security.jwt.JwtTokenProvider;
import com.techit.sociallogindemo.security.jwt.dto.JwtResponse;
import com.techit.sociallogindemo.security.jwt.entity.JwtRefreshToken;
import com.techit.sociallogindemo.security.jwt.repository.JwtRefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OAuth2Service {
	private final WebClient webClient;

	private final MemberRepository memberRepository;

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtRefreshTokenRepository refreshTokenRepository;

	private static final String KAKAO_USERINFO_ENDPOINT = "https://kapi.kakao.com/v2/user/me";

	public JwtResponse webClientTest(ProviderType provider, String accessToken) {
		OAuth2User oAuth2User = loadUser(provider, accessToken);
		OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
			oAuth2User,
			oAuth2User.getAuthorities(),
			provider.getValue()
		);

		JwtResponse jwtResponse = jwtTokenProvider.createAccessToken(authentication);
		JwtRefreshToken refreshToken = checkAndCreateRefreshToken(authentication);

		return buildJwtResponse(refreshToken, refreshToken.getMember(), jwtResponse);
	}

	public OAuth2User loadUser(ProviderType provider, String accessToken) {
		OAuth2UserInfo oAuth2UserInfo =
			OAuth2UserInfoFactory.getOAuth2UserInfo(provider, getAttributes(provider, accessToken));

		if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
			throw new OAuth2AuthenticationProcessingException("email not found from OAuth2 provider");
		}

		Member member = saveOAuth2Member(oAuth2UserInfo);

		return UserPrincipal.create(member, oAuth2UserInfo.getAttributes());
	}

	public Map<String, Object> getAttributes(ProviderType provider, String accessToken) {
		String endPoint = getEndPoint(provider);

		return webClient.post()
			.uri(endPoint)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
			})
			.block();
	}

	private String getEndPoint(ProviderType providerType) {
		return switch (providerType) {
			case KAKAO -> KAKAO_USERINFO_ENDPOINT;
			default -> throw new IllegalArgumentException("존재하지 않는 Provider");
		};
	}

	private Member saveOAuth2Member(OAuth2UserInfo oAuth2UserInfo) {
		return memberRepository.findByEmail(oAuth2UserInfo.getEmail())
			.orElseGet(() -> {
				Member member = Member.builder()
					.email(oAuth2UserInfo.getEmail())
					.name(oAuth2UserInfo.getName())
					.nickname(oAuth2UserInfo.getNickname())
					.profileImage(oAuth2UserInfo.getProfileImage())
					.provider(oAuth2UserInfo.getProvider().getValue())
					.build();

				return memberRepository.save(member);
			});
	}

	// 리프레시 토큰이 있는지 확인, 없으면 생성
	@Transactional
	protected JwtRefreshToken checkAndCreateRefreshToken(final Authentication authentication) {
		// 사용자 조회
		Member authenticateMember = findMember(authentication.getName());

		// 리프레시 토큰 조회
		JwtRefreshToken refreshToken = findRefreshToken(authenticateMember);

		// 리프레시 토큰 검증 및 생성
		return validateAndCreateRefreshToken(authentication, authenticateMember, refreshToken);
	}

	// 새로운 액세스 토큰 발급
	public JwtResponse newAccessToken(final String requestRefreshToken) {
		validateRefreshToken(requestRefreshToken);

		Authentication authentication = jwtTokenProvider.getAuthentication(requestRefreshToken);
		Member member = findMember(authentication.getName());
		JwtRefreshToken refreshToken = findRefreshToken(member);

		validateTokenMatch(requestRefreshToken, refreshToken);

		JwtResponse jwtResponse = jwtTokenProvider.createAccessToken(authentication);

		return buildJwtResponse(refreshToken, member, jwtResponse);
	}

	private void validateRefreshToken(final String requestRefreshToken) {
		if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
			throw new RuntimeException("Refresh Token 검증 실패");
		}
	}

	private JwtRefreshToken findRefreshToken(final Member authenticateMember) {
		return refreshTokenRepository.findByMember_Id(authenticateMember.getId()).orElse(null);
	}

	private void validateTokenMatch(final String requestRefreshToken, JwtRefreshToken refreshToken) {
		if (!refreshToken.getRefreshToken().equals(requestRefreshToken)) {
			throw new RuntimeException("토큰이 일치하지 않습니다.");
		}
	}

	private JwtRefreshToken validateAndCreateRefreshToken(final Authentication authentication,
		final Member authenticateMember,
		JwtRefreshToken refreshToken) {
		if (isTokenInvalid(refreshToken)) {
			refreshToken = recreateAndSaveRefreshToken(authentication, authenticateMember);
		}

		return refreshToken;
	}

	private boolean isTokenInvalid(final JwtRefreshToken refreshToken) {
		return refreshToken == null || !jwtTokenProvider.validateToken(refreshToken.getRefreshToken());
	}

	private JwtRefreshToken recreateAndSaveRefreshToken(final Authentication authentication,
		final Member authenticateMember) {
		log.info("유효하지 않을 때");

		JwtRefreshToken newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);
		return refreshTokenRepository.save(newRefreshToken.toBuilder().member(authenticateMember).build());
	}

	private JwtResponse buildJwtResponse(final JwtRefreshToken refreshToken,
		final Member member,
		final JwtResponse jwtResponse) {
		return jwtResponse.toBuilder()
			.refreshToken(refreshToken.getRefreshToken())
			.member(MemberResponse.of(member))
			.build();
	}

	private Member findMember(final String email) {
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new UserNotFoundException("회원을 찾을 수 없어요."));
	}

	/*public Mono<UserPrincipal> loadUser(ProviderType providerType, String accessToken) {
		String endPoint = getEndPoint(providerType);

		return webClient.get()
			.uri(endPoint)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(Map.class)  // 응답을 Map으로 받아옵니다.
			.map(attributes -> OAuthUserInfoFactory.getOAuth2UserInfo(providerType,
				attributes))  // Map을 OAuthUserInfo로 변환합니다.
			.flatMap(this::saveOrUpdate)  // User 정보를 저장하거나 업데이트합니다.
			.map(UserPrincipal::create);  // UserPrincipal 객체를 생성합니다.
	}

	private String getEndPoint(ProviderType providerType) {
		return switch (providerType) {
			case KAKAO -> KAKAO_USERINFO_ENDPOINT;
			default -> throw new IllegalArgumentException("존재하지 않는 Provider");
		};
	}

	@Async
	public CompletableFuture<Mono<Member>> saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {
		Member member = Member.builder()
			.email(oAuth2UserInfo.getEmail())
			.name(oAuth2UserInfo.getName())
			.nickname(oAuth2UserInfo.getNickname())
			.profileImage(oAuth2UserInfo.getProfileImage())
			.providerId(oAuth2UserInfo.getId())
			.provider(oAuth2UserInfo.getProvider().getValue())
			.build();

		return CompletableFuture.completedFuture(
			Mono.justOrEmpty(memberRepository.findByProviderId(oAuth2UserInfo.getId()))
				.switchIfEmpty(Mono.just(memberRepository.save(member))));
	}*/
}
