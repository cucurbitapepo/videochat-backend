package com.videochatapi.controller;

import com.videochatapi.dto.call.CallStatusRequest;
import com.videochatapi.dto.norification.NotificationDto;
import com.videochatapi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallWebSocketController {

  private final NotificationService notificationService;

  /**
   * Обработка статусов звонка через WebSocket
   * Клиент отправляет: /app/call/status
   *
   * Важно: статус REQUEST НЕ обрабатывается здесь,
   * так как уведомление отправляется в CallService.createCall() через REST.
   */
  @MessageMapping("/call/status")
  public void handleCallStatus(
          @Payload CallStatusRequest request,
          @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

    if (request.getStatus() == null) {
      log.error("Status is null in request");
      return;
    }

    String callId = request.getCallId();

    switch (request.getStatus().toUpperCase()) {
      case "REQUEST" -> {
        log.debug("REQUEST status received - handled via REST in CallService.createCall(), skipping WebSocket processing");

      }
      case "ACCEPTED" -> {
        log.debug("Sending ACCEPTED notification: callId={}, receiverId={}",
                request.getCallId(), request.getReceiverId());
        NotificationDto accepted = new NotificationDto(
                "CALL_STATUS",
                "accepted",
                callId,
                null,
                null,
                System.currentTimeMillis());

        notificationService.sendRoomNotification(callId, accepted);
      }
      case "REJECTED" -> {
        log.debug("Sending REJECTED notification: callId={}, receiverId={}",
                request.getCallId(), request.getReceiverId());

        notificationService.sendPrivateNotification(
                request.getReceiverId(),
                new NotificationDto("CALL_STATUS", "rejected", callId, null, null, System.currentTimeMillis())
        );
      }
      case "ENDED" -> {
        log.debug("Sending ENDED notification: callId={}, receiverId={}",
                request.getCallId(), request.getReceiverId());

        NotificationDto ended = new NotificationDto(
                "CALL_STATUS", "ended", callId, null, null, System.currentTimeMillis()
        );
        notificationService.sendRoomNotification(callId, ended);
      }
      default -> log.warn("Unknown call status: {}", request.getStatus());
    }
  }
}