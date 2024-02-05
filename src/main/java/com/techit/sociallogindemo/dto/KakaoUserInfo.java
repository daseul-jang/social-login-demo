package com.techit.sociallogindemo.dto;

import java.util.Map;

import com.techit.sociallogindemo.enums.ProviderType;

public class KakaoUserInfo extends OAuth2UserInfo {
	private Map<String, Object> account;
	private Map<String, Object> profile;

	public KakaoUserInfo(Map<String, Object> attributes) {
		super(attributes);
		this.account = (Map<String, Object>)attributes.get("kakao_account");
		this.profile = (Map<String, Object>)account.get("profile");
	}

	@Override
	public String getName() {
		return (String)account.get("name");
	}

	@Override
	public String getEmail() {
		return (String)account.get("email");
	}

	@Override
	public String getNickname() {
		return (String)profile.get("nickname");
	}

	@Override
	public String getProfileImage() {
		return (String)profile.get("profile_image_url");
	}

	@Override
	public ProviderType getProvider() {
		return ProviderType.KAKAO;
	}
}
