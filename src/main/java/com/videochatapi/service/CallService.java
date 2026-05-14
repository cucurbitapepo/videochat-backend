package com.videochatapi.service;

import com.videochatapi.dto.call.CallDto;
import com.videochatapi.dto.livekit.RoomTokenResponse;
import com.videochatapi.exception.CallNotFoundException;
import com.videochatapi.model.Call;
import com.videochatapi.model.CallStatus;
import com.videochatapi.model.User;
import com.videochatapi.props.AppProperties;
import com.videochatapi.repository.CallRepository;
import com.videochatapi.repository.UserRepository;
import com.videochatapi.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CallService {

  private final CallRepository callRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final CurrentUserProvider currentUserProvider;
  private final LiveKitService liveKitService;
  private final LiveKitTokenService liveKitTokenService;

  private final AppProperties appProperties;


  @Transactional(readOnly = true)
  public CallDto getCallById(String callId) {
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));
    return CallDto.fromEntity(call);
  }

  public CallDto createCall(CallDto callDto) {
    User caller = currentUserProvider.getCurrentUser();

    String roomName = callDto.getCallId() != null && !callDto.getCallId().isEmpty()
            ? callDto.getCallId()
            : "call-" + System.currentTimeMillis();

    try {
      liveKitService.createRoom(roomName, 10);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create LiveKit room", e);
    }

    Call call = new Call();
    call.setCallId(roomName);
    call.setCaller(caller);
    call.setStatus(CallStatus.WAITING);

    call.addParticipant(caller);

    String initiatorDhPublicKey = callDto.getInitiatorDhPublicKey();

    callDto.getParticipants().forEach(userId -> {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
      call.addParticipant(user);
      notificationService.sendCallRequest(
              caller.getId(),
              user.getId(),
              call.getCallId(),
              caller.getUsername(),
              initiatorDhPublicKey
      );
    });

    Call savedCall = callRepository.save(call);

    return CallDto.fromEntity(savedCall);
  }

  public CallDto acceptCall(String callId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));

    if (!call.isParticipant(currentUser)) {
      throw new IllegalArgumentException("User is not a participant of this call");
    }

    call.setStatus(CallStatus.ACTIVE);
    Call savedCall = callRepository.save(call);

    notificationService.sendCallStatus(call.getCallId(), "accepted", call.getCaller().getId());

    return CallDto.fromEntity(savedCall);
  }

  public CallDto rejectCall(String callId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));

    if (!call.isParticipant(currentUser)) {
      throw new IllegalArgumentException("User is not a participant of this call");
    }

    call.setStatus(CallStatus.REJECTED);
    call.setEndedAt(LocalDateTime.now());
    Call savedCall = callRepository.save(call);

    notificationService.sendCallStatus(call.getCallId(), "rejected", call.getCaller().getId());

    return CallDto.fromEntity(savedCall);
  }

  public CallDto cancelCall(String callId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));

    if (!call.getCaller().getId().equals(currentUser.getId())) {
      throw new IllegalArgumentException("Only caller can cancel the call");
    }
    if (call.getStatus() != CallStatus.WAITING) {
      throw new IllegalStateException("Cannot cancel call with status: " + call.getStatus());
    }

    call.setStatus(CallStatus.CANCELLED);
    call.setEndedAt(LocalDateTime.now());
    Call savedCall = callRepository.save(call);

    call.getParticipants().stream()
            .filter(user -> !user.getId().equals(currentUser.getId()))
            .forEach(user ->
                    notificationService.sendCallStatus(call.getCallId(), "cancelled", user.getId())
            );

    try {
      liveKitService.deleteRoom(callId);
    } catch (Exception e) {
      log.warn("Failed to delete LiveKit room on cancel: {}", callId, e);
    }

    return CallDto.fromEntity(savedCall);
  }

  public CallDto endCall(String callId) {
    User currentUser = currentUserProvider.getCurrentUser();
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));

    if (!call.isParticipant(currentUser)) {
      throw new IllegalArgumentException("User is not a participant of this call");
    }

    call.setStatus(CallStatus.ENDED);
    call.setEndedAt(LocalDateTime.now());
    Call savedCall = callRepository.save(call);

    call.getParticipants().stream()
            .filter(user -> !user.getId().equals(currentUser.getId()))
            .forEach(user ->
                    notificationService.sendCallStatus(call.getCallId(), "ended", user.getId()));

    try {
      liveKitService.deleteRoom(callId);
    } catch (Exception e) {
      log.error("Failed to delete LiveKit room: {}", callId, e);
    }

    return CallDto.fromEntity(savedCall);
  }

  @Transactional(readOnly = true)
  public List<CallDto> getActiveCalls() {
    User currentUser = currentUserProvider.getCurrentUser();
    return callRepository.findByParticipantsContainingAndStatusNot(currentUser, CallStatus.ENDED)
            .stream()
            .map(CallDto::fromEntity)
            .toList();
  }

  public RoomTokenResponse getCallToken(String callId, boolean isPublisher) {
    User currentUser = currentUserProvider.getCurrentUser();
    Call call = callRepository.findByCallId(callId)
            .orElseThrow(() -> new CallNotFoundException("Call not found"));

    if (!call.isParticipant(currentUser)) {
      throw new IllegalArgumentException("User is not a participant of this call");
    }

    String token = liveKitTokenService.generateRoomToken(
            currentUser.getUsername(),
            callId,
            isPublisher
    );

    RoomTokenResponse response = new RoomTokenResponse();
    response.setToken(token);
    response.setRoomName(callId);
    response.setServerUrl(appProperties.getLivekit().getExternalUrl());

    return response;
  }

  @Transactional(readOnly = true)
  public boolean isParticipant(String callId, String username) {
    return callRepository.findByCallId(callId)
            .map(call -> call.isParticipantByUsername(username))
            .orElse(false);
  }

  @Transactional(readOnly = true)
  public boolean canDistributeKeys(String callId, String username) {
    Call call = callRepository.findByCallId(callId).orElse(null);
    if (call == null) return false;

    if (call.getCaller() != null && call.getCaller().getUsername().equals(username)) {
      return true;
    }

    return call.isParticipantByUsername(username) && call.getStatus() == CallStatus.ACTIVE;
  }

  @Transactional(readOnly = true)
  public List<String> getParticipantUsernames(String callId) {
    return callRepository.findByCallId(callId)
            .map(call -> call.getParticipants().stream()
                    .map(User::getUsername)
                    .toList())
            .orElse(List.of());
  }
}
