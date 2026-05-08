package com.videochatapi.dto.e2ee;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrappedGroupKeyMessage {
  @JsonProperty("inviterId")
  private String inviterId;

  @JsonProperty("recipientId")
  private String recipientId;

  @JsonProperty("wrappedKeyBase64")
  private String wrappedKeyBase64;

  @JsonProperty("nonceBase64")
  private String nonceBase64;

  @JsonProperty("keyIndex")
  private Integer keyIndex;

  @JsonProperty("callId")
  private String callId;

  @JsonProperty("timestamp")
  private Long timestamp;

  public WrappedGroupKeyMessage(String inviterId, String recipientId,
                                String wrappedKeyBase64, String nonceBase64,
                                Integer keyIndex, String callId) {
    this.inviterId = inviterId;
    this.recipientId = recipientId;
    this.wrappedKeyBase64 = wrappedKeyBase64;
    this.nonceBase64 = nonceBase64;
    this.keyIndex = keyIndex;
    this.callId = callId;
    this.timestamp = System.currentTimeMillis();
  }
}

