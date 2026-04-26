package com.videochatapi.config;

import com.videochatapi.security.JwtTokenProvider;
import com.videochatapi.service.UserPresenceService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UserChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserPresenceService userPresenceService;

  public UserChannelInterceptor(JwtTokenProvider jwtTokenProvider, UserPresenceService userPresenceService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userPresenceService = userPresenceService;
  }


  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      Long userId = (Long) accessor.getSessionAttributes().get("userId");

      if (userId != null) {
        accessor.getSessionAttributes().put("userId", userId);
        userPresenceService.userConnected(userId);
      }

      UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userId, null, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      accessor.setNativeHeader("userId", userId.toString());
    } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
      Long userId = getUserIdFromSession(accessor);
      if (userId != null) {
        userPresenceService.userDisconnected(userId);
      }
    }

    return message;
  }

  private Long getUserIdFromSession(StompHeaderAccessor accessor) {
    Object userIdObj = accessor.getSessionAttributes().get("userId");
    return (userIdObj instanceof Long) ? (Long) userIdObj : null;
  }

  private String getAuthTokenFromHeader(StompHeaderAccessor accessor) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }

    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    if (sessionAttributes != null) {
      Object tokenParam = sessionAttributes.get("token");
      if (tokenParam != null) {
        return tokenParam.toString();
      }
    }

    return null;
  }
}
