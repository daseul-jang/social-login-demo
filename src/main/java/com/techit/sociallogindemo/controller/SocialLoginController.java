package com.techit.sociallogindemo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techit.sociallogindemo.enums.ProviderType;
import com.techit.sociallogindemo.service.OAuth2Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class SocialLoginController {
	private final OAuth2Service oAuth2Service;

	@PostMapping("/{provider}")
	public ResponseEntity<?> oAuthLogin(@PathVariable String provider, @RequestBody String accessToken) {
		log.info("provider: {}, accessToken: {}", provider, accessToken);

		try {
			return ResponseEntity.ok(oAuth2Service.webClientTest(ProviderType.fromString(provider), accessToken));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 실패");
		}
	}
}
