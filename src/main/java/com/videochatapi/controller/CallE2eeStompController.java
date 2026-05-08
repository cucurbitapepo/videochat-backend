package com.videochatapi.controller;

import com.videochatapi.dto.e2ee.DhPublicKeyMessage;
import com.videochatapi.dto.e2ee.E2eeReadyMessage;
import com.videochatapi.dto.e2ee.WrappedGroupKeyMessage;
import com.videochatapi.service.CallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CallE2eeStompController {

  private final SimpMessagingTemplate messagingTemplate;
  private final CallService callService;

  @MessageMapping("/call/{callId}/e2ee/dh-key")
  public void handleDhPublicKey(
          @Payload DhPublicKeyMessage message,
          @Header("simpSessionAttributes") Map<String, Object> sessionAttributes,
          StompHeaderAccessor accessor) {

    String callId = message.getCallId();
    String senderUsername = extractUsername(accessor);

    log.debug("Received DH public key from {} for call {}", senderUsername, callId);

    if (!callService.isParticipant(callId, senderUsername)) {
      log.warn("User {} is not a participant of call {}, rejecting DH key", senderUsername, callId);
      throw new AccessDeniedException("Not a participant of this call");
    }

    messagingTemplate.convertAndSend(
            "/topic/call/" + callId + "/e2ee/dh-key",
            message
    );
    log.debug("Broadcasted DH key from {} to all participants of call {}", senderUsername, callId);
  }

  @MessageMapping("/call/{callId}/e2ee/wrapped-key")
  public void handleWrappedGroupKey(
          @Payload WrappedGroupKeyMessage message,
          StompHeaderAccessor accessor) {

    String callId = message.getCallId();
    String senderUsername = extractUsername(accessor);
    String recipientUsername = message.getRecipientId();

    log.debug("Received wrapped group key from {} for {} in call {}",
            senderUsername, recipientUsername, callId);

    if (!callService.canDistributeKeys(callId, senderUsername)) {
      log.warn("User {} is not authorized to distribute keys in call {}", senderUsername, callId);
      throw new AccessDeniedException("Not authorized to distribute E2EE keys");
    }

    if (!callService.isParticipant(callId, recipientUsername)) {
      log.warn("Recipient {} is not a participant of call {}, rejecting wrapped key", recipientUsername, callId);
      throw new AccessDeniedException("Recipient is not a participant of this call");
    }

    messagingTemplate.convertAndSendToUser(
            recipientUsername,
            "/queue/e2ee/wrapped-key",
            message
    );
    log.debug("Sent wrapped key from {} to user {} in call {}", senderUsername, recipientUsername, callId);
  }

  @MessageMapping("/call/{callId}/e2ee/ready")
  public void handleE2eeReady(
          @Payload E2eeReadyMessage message,
          StompHeaderAccessor accessor) {

    String callId = message.getCallId();
    String senderUsername = extractUsername(accessor);

    log.debug("Received E2EE ready signal from {} for call {}", senderUsername, callId);

    if (!callService.isParticipant(callId, senderUsername)) {
      log.warn("User {} is not a participant of call {}, rejecting ready signal", senderUsername, callId);
      throw new AccessDeniedException("Not a participant of this call");
    }

    messagingTemplate.convertAndSend(
            "/topic/call/" + callId + "/e2ee/ready",
            message
    );
    log.debug("Broadcasted ready signal from {} to all participants of call {}", senderUsername, callId);
  }

  private String extractUsername(StompHeaderAccessor accessor) {
    Principal principal = accessor.getUser();
    if (principal != null) {
      return principal.getName();
    }
    Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
    if (sessionAttrs != null && sessionAttrs.containsKey("username")) {
      return (String) sessionAttrs.get("username");
    }
    throw new IllegalStateException("Could not extract username from STOMP session");
  }
}
