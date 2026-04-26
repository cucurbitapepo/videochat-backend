package com.videochatapi.controller;


import com.videochatapi.dto.fcm.FcmTokenDto;
import com.videochatapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class FcmTokenController {

  private final UserService userService;

  @PostMapping("/fcm-token")
  public ResponseEntity<Void> updateFcmToken(
          @AuthenticationPrincipal Long userId,
          @RequestBody FcmTokenDto request) {

    userService.updateFcmToken(userId, request.getToken(), request.getExpiry());
    return ResponseEntity.ok().build();
  }
}