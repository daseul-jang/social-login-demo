package com.techit.sociallogindemo.enums;

import lombok.Getter;

@Getter
public enum ProviderType {
	KAKAO("kakao");

	private final String value;

	ProviderType(String value) {
		this.value = value;
	}

	public static ProviderType fromString(String value) {
		for (ProviderType type : ProviderType.values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}

		throw new IllegalArgumentException("No Provider");
	}
}
