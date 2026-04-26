package com.videochatapi.config;

import com.videochatapi.security.JwtRequestFilter;
import com.videochatapi.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

  private final JwtRequestFilter jwtRequestFilter;
  private final CustomUserDetailsService customUserDetailsService;

  private final int HASHING_STRENGTH = 12;

  @Value("${cors.allowed-origins}")
  private String corsOrigins;
  @Value("${cors.allowed-headers}")
  private String corsHeaders;
  @Value("${cors.allowed-methods}")
  private String corsMethods;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS).permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(handler -> handler
                    .authenticationEntryPoint((request, response, authException) -> {
                      response.setStatus(HttpStatus.UNAUTHORIZED.value());
                      response.getWriter().write("Unauthorized");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                      response.setStatus(HttpStatus.FORBIDDEN.value());
                      response.getWriter().write("You do not have permission to access this resource");
                    }))
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();

    corsConfiguration.setAllowedOrigins(Arrays.asList(
            corsOrigins.split(",")
    ));

    corsConfiguration.setAllowedMethods(Arrays.asList(
            corsMethods.split(",")
    ));
    corsConfiguration.setAllowedHeaders(Arrays.asList(
            corsHeaders.split(",")
    ));
    corsConfiguration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/v1/**", corsConfiguration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationConfiguration authenticationConfiguration
  ) {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(HASHING_STRENGTH);
  }
}
