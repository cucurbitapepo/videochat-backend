package com.videochatapi.dto.e2ee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DhPublicKeyMessage {
  @JsonProperty("senderId")
  private String senderId;

  @JsonProperty("publicKeyBase64")
  private String publicKeyBase64;

  @JsonProperty("callId")
  private String callId;

  @JsonProperty("timestamp")
  private Long timestamp;

  public DhPublicKeyMessage(String senderId, String publicKeyBase64, String callId) {
    this.senderId = senderId;
    this.publicKeyBase64 = publicKeyBase64;
    this.callId = callId;
    this.timestamp = System.currentTimeMillis();
  }
}
