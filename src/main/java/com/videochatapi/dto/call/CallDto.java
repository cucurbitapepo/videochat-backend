package com.videochatapi.dto.call;

import com.videochatapi.model.Call;
import com.videochatapi.model.User;
import com.videochatapi.model.CallStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CallDto {
  private String callId;
  private Long callerId;
  private Set<Long> participants;
  private CallStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime endedAt;
  private String initiatorDhPublicKey;

  public static CallDto createRequest(Long receiverId) {
    CallDto dto = new CallDto();
    dto.setParticipants(Set.of(receiverId));
    return dto;
  }

  public static CallDto fromEntity(Call call) {
    CallDto dto = new CallDto();
    dto.setCallId(call.getCallId());
    dto.setCallerId(call.getCaller().getId());
    dto.setParticipants(call.getParticipants().stream()
            .map(User::getId)
            .collect(Collectors.toSet()));
    dto.setStatus(call.getStatus());
    dto.setCreatedAt(call.getCreatedAt());
    dto.setEndedAt(call.getEndedAt());
    return dto;
  }
}
