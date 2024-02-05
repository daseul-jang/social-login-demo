package com.techit.sociallogindemo.dto;

import java.util.Map;

import com.techit.sociallogindemo.enums.ProviderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class OAuth2UserInfo {
	protected Map<String, Object> attributes;

	public abstract String getName();

	public abstract String getEmail();

	public abstract String getNickname();

	public abstract String getProfileImage();

	public abstract ProviderType getProvider();
}
