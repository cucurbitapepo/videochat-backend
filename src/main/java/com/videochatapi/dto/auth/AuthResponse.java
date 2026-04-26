package com.videochatapi.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
  private String accessToken;
  private String tokenType = "Bearer";
  private Long expiresIn;
}
