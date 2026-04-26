package com.videochatapi.dto.contact;

import com.videochatapi.dto.user.UserDto;
import lombok.Data;

@Data
public class ContactDto {
  private Long id;
  private UserDto contact;
  private String alias;
  private String createdAt;
  private String updatedAt;

}