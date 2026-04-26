package com.videochatapi.config;

import com.videochatapi.security.JwtAuthHandshakeInterceptor;
import com.videochatapi.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtWebSocketConfig {

  @Bean
  public JwtAuthHandshakeInterceptor jwtAuthHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
    return new JwtAuthHandshakeInterceptor(jwtTokenProvider);
  }
}
