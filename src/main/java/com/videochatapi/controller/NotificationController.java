package com.videochatapi.controller;

import com.videochatapi.security.CurrentUserProvider;
import com.videochatapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final CurrentUserProvider currentUserProvider;

}
