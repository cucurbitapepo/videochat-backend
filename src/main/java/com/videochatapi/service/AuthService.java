package com.videochatapi.service;

import com.videochatapi.dto.auth.AuthRequest;
import com.videochatapi.dto.auth.AuthResponse;
import com.videochatapi.model.User;
import com.videochatapi.model.UserRole;
import com.videochatapi.repository.UserRepository;
import com.videochatapi.security.JwtProperties;
import com.videochatapi.security.JwtTokenProvider;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProperties jwtProperties;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;

  public AuthResponse register(AuthRequest authRequest) {
    if (userRepository.existsByUsername(authRequest.getUsername())) {
      throw new IllegalArgumentException("Имя пользователя уже занято.");
    }

    User user = new User();
    user.setUsername(authRequest.getUsername());
    user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
    user.setRoles(Collections.setOf(UserRole.USER));

    userRepository.save(user);

    return generateToken(user);
  }

  public AuthResponse login(AuthRequest authRequest) {
    try {
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      authRequest.getUsername(),
                      authRequest.getPassword()
              )
      );
    } catch (BadCredentialsException e) {
      throw new IllegalArgumentException("Неверные учётные данные");
    }
    User user = userService.getByUsername(authRequest.getUsername());

    return generateToken(user);
  }

  private AuthResponse generateToken(User user) {
    String token = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRoles());
    return new AuthResponse(
            token,
            "Bearer",
            jwtProperties.getExpiration() / 1000
    );
  }

  public void saveFcmToken(String token, String authToken) {
    String username = jwtTokenProvider.extractUsername(authToken);
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    LocalDateTime expiry = LocalDateTime.now().plusDays(30);

    userRepository.updateFcmToken(user.getId(), token, expiry);
  }
}
