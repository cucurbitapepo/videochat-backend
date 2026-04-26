package com.videochatapi.mapping;

import com.videochatapi.dto.user.UserDto;
import com.videochatapi.model.User;
import com.videochatapi.service.UserPresenceService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "online", ignore = true)
  UserDto toUserDto(User user);

  @Named("computeOnlineStatus")
  default boolean computeOnlineStatus(User user, UserPresenceService presenceService) {
    return presenceService != null && presenceService.isUserOnline(user.getId());
  }


  User toEntity(UserDto dto);
}
