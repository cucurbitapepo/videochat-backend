package com.videochatapi.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class LiveKitTokenService {

  private final String apiKey;
  private final String apiSecret;

  public LiveKitTokenService(
          @Value("${app.livekit.api-key}") String apiKey,
          @Value("${app.livekit.api-secret}") String apiSecret) {
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  /**
   * Генерирует токен доступа к комнате LiveKit
   *
   * @param identity Уникальный идентификатор пользователя в комнате
   * @param roomName Название комнаты
   * @param isPublisher Является ли пользователь публикатором
   * @return Сгенерированный JWT токен
   */
  public String generateRoomToken(String identity, String roomName, boolean isPublisher) {
    SecretKey key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));

    Map<String, Object> claims = new HashMap<>();

    Map<String, Object> video = new HashMap<>();
    video.put("roomJoin", true);
    video.put("room", roomName);
    if (isPublisher) {
      video.put("roomAdmin", true);
    }
    claims.put("video", video);

    claims.put("iss", apiKey);
    claims.put("nbf", System.currentTimeMillis() / 1000);
    claims.put("exp", (System.currentTimeMillis() / 1000) + 300);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(identity)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * Генерирует API-токен для сервер-сервер взаимодействия с LiveKit Server
   * Используется для вызовов API: createRoom, deleteRoom, listRooms
   */
  public String generateApiToken() {
    SecretKey key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));

    Map<String, Object> video = new HashMap<>();
    video.put("roomCreate", true);
    video.put("roomList", true);
    video.put("roomRecord", true);
    video.put("roomAdmin", true);

    Map<String, Object> claims = new HashMap<>();
    claims.put("video", video);
    claims.put("iss", apiKey);
    claims.put("sub", apiKey);
    claims.put("nbf", System.currentTimeMillis() / 1000);
    claims.put("exp", (System.currentTimeMillis() / 1000) + 300);

    return Jwts.builder()
            .setClaims(claims)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
  }
}