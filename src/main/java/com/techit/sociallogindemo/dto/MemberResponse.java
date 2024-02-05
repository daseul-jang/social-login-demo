package com.techit.sociallogindemo.dto;

import com.techit.sociallogindemo.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
	private Long id;
	private String email;
	private String name;
	private String nickname;
	private String profileImage;
	private String provider;

	public static MemberResponse of(Member member) {
		return MemberResponse.builder()
			.id(member.getId())
			.email(member.getEmail())
			.name(member.getName())
			.nickname(member.getNickname())
			.profileImage(member.getProfileImage())
			.provider(member.getProvider())
			.build();
	}
}
