package com.videochatapi.dto.e2ee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class E2eeReadyMessage {
  @JsonProperty("userId")
  private String userId;

  @JsonProperty("callId")
  private String callId;

  @JsonProperty("ready")
  private Boolean ready;

  @JsonProperty("timestamp")
  private Long timestamp;

  public E2eeReadyMessage(String userId, String callId, Boolean ready) {
    this.userId = userId;
    this.callId = callId;
    this.ready = ready != null ? ready : true;
    this.timestamp = System.currentTimeMillis();
  }
}
