package com.techit.sociallogindemo.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.techit.sociallogindemo.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvcRequestMatcher) throws
		Exception {
		http
			.cors(cors -> cors.configure(http))
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(authorizeRequests -> authorizeRequests
				.requestMatchers(mvcRequestMatcher.pattern("/**"))
				.permitAll()
			)
			.sessionManagement(
				sessionManagement -> sessionManagement
					.sessionCreationPolicy(
						SessionCreationPolicy.STATELESS
					)
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring()
			.requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
			.requestMatchers(new AntPathRequestMatcher("/favicon.ico"))
			.requestMatchers(new AntPathRequestMatcher("/css/**"))
			.requestMatchers(new AntPathRequestMatcher("/js/**"))
			.requestMatchers(new AntPathRequestMatcher("/img/**"))
			.requestMatchers(new AntPathRequestMatcher("/lib/**"));
	}

	@Bean
	protected MvcRequestMatcher.Builder mvcRequestMatcherBuilder(HandlerMappingIntrospector introspect) {
		return new MvcRequestMatcher.Builder(introspect);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
		Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
