package com.videochatapi.service;

import com.videochatapi.dto.norification.NotificationDto;
import com.videochatapi.model.User;
import com.videochatapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
  private final SimpMessagingTemplate messagingTemplate;
  private final UserPresenceService userPresenceService;
  private final UserRepository userRepository;
  private final FcmNotificationService fcmNotificationService;

  public void sendNotificationToUser(Long userId, NotificationDto notification) {
    User user = userRepository.findById(userId).orElse(null);

    if (user == null) {
      log.warn("User {} not found, skipping notification: {}", userId, notification.getType());
      return;
    }


    if (userPresenceService.isUserOnline(userId)) {
      messagingTemplate.convertAndSendToUser(
              user.getUsername(),
              "/queue/notifications",
              notification
      );
    } else {
      fcmNotificationService.sendNotificationToUser(userId, notification);
    }
  }

  public void sendCallRequest(Long callerId, Long receiverId, String callId, String callerName) {

    NotificationDto notification = new NotificationDto(
            "CALL_REQUEST",
            "Входящий звонок",
            callId,
            callerId,
            callerName,
            System.currentTimeMillis()
    );

    sendNotificationToUser(receiverId, notification);
  }

  public void sendCallStatus(String callId, String status, Long userId) {
    String type = "CALL_STATUS";

    NotificationDto notification = new NotificationDto(
            type,
            status,
            callId,
            null,
            null,
            System.currentTimeMillis()
    );

    sendNotificationToUser(userId, notification);
  }

  public void sendPrivateNotification(Long userId, NotificationDto notification) {
    if (userPresenceService.isUserOnline(userId)) {
      messagingTemplate.convertAndSendToUser(
              userId.toString(),
              "/queue/notifications",
              notification
      );
    } else {
      fcmNotificationService.sendNotificationToUser(userId, notification);
    }
  }

  /**
   * Отправляет уведомление всем участникам комнаты
   * Используется для CALL_STATUS (accepted, ended)
   */
  public void sendRoomNotification(String roomId, NotificationDto notification) {
    messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
  }

}
