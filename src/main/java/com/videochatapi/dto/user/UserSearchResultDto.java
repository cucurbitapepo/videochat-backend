package com.videochatapi.dto.user;

import com.videochatapi.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSearchResultDto {

  private Long id;
  private String username;
  private boolean online;
  private boolean isContact;

  public static UserSearchResultDto fromEntity(User user) {
    return new UserSearchResultDtoBuilder()
            .id(user.getId())
            .username(user.getUsername())
            .online(false)
            .build();
  }
}
