package com.videochatapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserPresenceService {

  private final Set<Long> onlineUsers = new HashSet<>();

  /**
   * Регистрация пользователя как онлайн при подключении WebSocket
   */
  public void userConnected(Long userId) {
    if (userId != null) {
      onlineUsers.add(userId);
    }
    log.info("User {} connected", userId);
  }

  /**
   * Удаление пользователя из списка онлайн при отключении
   */
  public void userDisconnected(Long userId) {
    if (userId != null) {
      onlineUsers.remove(userId);
    }
  }

  /**
   * Проверка, онлайн ли пользователь
   */
  public boolean isUserOnline(Long userId) {
    return userId != null && onlineUsers.contains(userId);
  }

}
