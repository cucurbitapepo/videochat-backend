package com.videochatapi.dto.user;

import lombok.Data;


@Data
public class UserDto {
  private Long id;
  private String username;
  private boolean online;
  private String createdAt;

}