package com.techit.sociallogindemo.service;

import java.util.Map;

import com.techit.sociallogindemo.dto.KakaoUserInfo;
import com.techit.sociallogindemo.dto.OAuth2UserInfo;
import com.techit.sociallogindemo.enums.ProviderType;
import com.techit.sociallogindemo.exception.OAuth2AuthenticationProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(ProviderType providerType, Map<String, Object> attributes) {
		return switch (providerType) {
			case KAKAO -> {
				log.info("카카오 로그인 정보 리턴");
				yield new KakaoUserInfo(attributes);
			}
			default -> throw new OAuth2AuthenticationProcessingException("지원하지 않는 소셜 로그인 입니다.");
		};
	}
}
