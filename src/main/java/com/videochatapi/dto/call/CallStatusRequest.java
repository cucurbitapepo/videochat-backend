package com.videochatapi.dto.call;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallStatusRequest {
  private String status;
  private String callId;
  private Long receiverId;
}