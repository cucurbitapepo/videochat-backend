package com.videochatapi.dto.norification;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationDto {
  private String type;
  private String message;
  private String data;
  private Long callerId;
  private String callerName;
  private long timestamp;
  private String initiatorDhPublicKey;

  public NotificationDto(String type, String message, String data, Long callerId, String callerName, long timestamp, String initiatorDhPublicKey) {
    this.type = type;
    this.message = message;
    this.data = data;
    this.callerId = callerId;
    this.callerName = callerName;
    this.timestamp = timestamp;
    this.initiatorDhPublicKey = initiatorDhPublicKey;
  }

  public NotificationDto(String type, String message, String data,
                         Long callerId, String callerName, long timestamp) {
    this(type, message, data, callerId, callerName, timestamp, null);
  }
}
