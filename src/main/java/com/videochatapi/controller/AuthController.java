package com.videochatapi.controller;

import com.videochatapi.dto.auth.AuthRequest;
import com.videochatapi.dto.auth.AuthResponse;
import com.videochatapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
          @Valid
          @RequestBody
          AuthRequest authRequest
  ) {
    return ResponseEntity.ok(authService.register(authRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
          @Valid
          @RequestBody
          AuthRequest authRequest
  ) {
    return ResponseEntity.ok(authService.login(authRequest));
  }
}
