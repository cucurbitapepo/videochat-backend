package com.videochatapi.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.http.Cookie;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.WebUtils;

import java.util.Map;

public class JwtAuthHandshakeInterceptor implements HandshakeInterceptor {

  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      String authToken = getAuthTokenFromRequest(servletRequest);

      try {
        if (authToken != null && jwtTokenProvider.validateToken(authToken)) {
          Long userId = jwtTokenProvider.extractUserId(authToken, Long.class);
          attributes.put("userId", userId);
          return true;
        }
      } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
      }
    }
    return false;
  }

  private String getAuthTokenFromRequest(ServletServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    String tokenParam = request.getServletRequest().getParameter("token");
    if (tokenParam != null) {
      return tokenParam;
    }

    Cookie cookie = WebUtils.getCookie(request.getServletRequest(), "auth_token");
    if (cookie != null) {
      return cookie.getValue();
    }

    return null;
  }

  @Override
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
  }
}
