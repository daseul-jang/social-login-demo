package com.techit.sociallogindemo.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
	@Bean
	public ReactorResourceFactory resourceFactory() {
		ReactorResourceFactory factory = new ReactorResourceFactory();
		factory.setUseGlobalResources(false);
		return factory;
	}

	@Bean
	public WebClient webClient() {
		Function<HttpClient, HttpClient> mapper = client -> client
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
			.doOnConnected(connection -> connection
				.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
				.addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)))
			.responseTimeout(Duration.ofSeconds(1));

		ClientHttpConnector connector = new ReactorClientHttpConnector(resourceFactory(), mapper);

		return WebClient.builder().clientConnector(connector).build();
	}
}
