package com.videochatapi.dto.livekit;

import lombok.Data;

@Data
public class RoomTokenResponse {
  private String token;
  private String roomName;
  private String serverUrl;
}