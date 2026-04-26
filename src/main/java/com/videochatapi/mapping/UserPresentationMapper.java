package com.videochatapi.mapping;

import com.videochatapi.dto.user.UserDto;
import com.videochatapi.dto.user.UserSearchResultDto;
import com.videochatapi.model.User;
import com.videochatapi.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPresentationMapper {

  private final UserPresenceService userPresenceService;
  private final UserMapper userMapper;

  /**
   * Конвертирует User → UserDto с вычислением онлайн-статуса
   */
  public UserDto toDtoWithOnline(User user) {
    UserDto dto = userMapper.toUserDto(user);
    dto.setOnline(userPresenceService.isUserOnline(user.getId()));
    return dto;
  }

  /**
   * Конвертирует User → UserSearchResultDto с онлайн-статусом
   */
  public UserSearchResultDto toSearchResultDto(User user) {
    return UserSearchResultDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .online(userPresenceService.isUserOnline(user.getId()))
            .build();
  }
}