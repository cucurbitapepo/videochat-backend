package com.videochatapi.service;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.*;
import com.videochatapi.dto.norification.NotificationDto;
import com.videochatapi.model.User;
import com.videochatapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {

  private final UserRepository userRepository;
  private final FirebaseMessaging firebaseMessaging;

  public void sendNotificationToUser(Long userId, NotificationDto notification) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getFcmToken() == null || isFcmTokenExpired(user)) {
      return;
    }

    Message message = buildMessage(user.getFcmToken(), notification);
    try {
      firebaseMessaging.send(message);
    } catch (FirebaseMessagingException e) {
      handleFcmError(user, e);
    }
  }

  private Message buildMessage(String fcmToken, NotificationDto notification) {
    return Message.builder()
            .setToken(fcmToken)
            .putAllData(Collections.singletonMap("type", notification.getType()))
            .putAllData(Collections.singletonMap("callId", notification.getData()))
            .putAllData(Collections.singletonMap("click_action", "CALL_NOTIFICATION"))
            .setNotification(Notification.builder()
                    .setTitle("Входящий звонок")
                    .setBody("Нажмите, чтобы ответить")
                    .build())
            .setAndroidConfig(AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("video_call_channel")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .setVisibility(AndroidNotification.Visibility.PUBLIC)
                            .setSound("default")
                            .setVibrateTimingsInMillis(new long[]{0L, 500L, 500L})
                            .setDefaultVibrateTimings(true)
                            .build())
                    .setDirectBootOk(true)
                    .build())
            .setApnsConfig(ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setContentAvailable(true)
                            .setSound("default")
                            .setAlert(ApsAlert.builder()
                                    .setTitle("Входящий звонок")
                                    .setBody("Нажмите, чтобы ответить")
                                    .build())
                            .setCategory("CALL_CATEGORY")
                            .setThreadId("video_call")
                            .build())
                    .putHeader("apns-priority", "10")
                    .build())
            .build();
  }

  private boolean isFcmTokenExpired(User user) {
    return user.getFcmTokenExpiry() != null &&
           user.getFcmTokenExpiry().isBefore(LocalDateTime.now());
  }

  private void handleFcmError(User user, Exception e) {
    System.err.println("FCM error for user " + user.getId() + ": " + e.getMessage());

    if (e instanceof FirebaseMessagingException) {
      FirebaseMessagingException fcmEx = (FirebaseMessagingException) e;
      if (fcmEx.getErrorCode() == ErrorCode.UNAUTHENTICATED ||
          fcmEx.getErrorCode() == ErrorCode.INVALID_ARGUMENT) {
        clearFcmToken(user);
      }
    }
  }

  private void clearFcmToken(User user) {
    userRepository.updateFcmToken(user.getId(), null, null);
  }
}