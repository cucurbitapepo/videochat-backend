package com.videochatapi.dto.fcm;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FcmTokenDto {
  private String token;
  private LocalDateTime expiry;
}
