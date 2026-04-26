package com.videochatapi.service;

import com.videochatapi.dto.user.UserDto;
import com.videochatapi.dto.user.UserSearchResultDto;
import com.videochatapi.mapping.UserPresentationMapper;
import com.videochatapi.model.User;
import com.videochatapi.repository.UserRepository;
import com.videochatapi.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private static final int SEARCH_LIMIT = 20;
  private static final int MIN_SEARCH_LENGTH = 3;

  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;
  private final UserPresentationMapper userPresentationMapper;

  public void updateFcmToken(Long userId, String token, LocalDateTime expiry) {
    userRepository.updateFcmToken(userId, token, expiry);
  }

  public User getByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
  }

  public UserDto toDtoWithOnline(User user) {
    return userPresentationMapper.toDtoWithOnline(user);
  }

  public UserSearchResultDto toSearchResultDto(User user) {
    return userPresentationMapper.toSearchResultDto(user);
  }

  public List<UserSearchResultDto> searchUsers(String query) {
    if (query == null || query.trim().length() < MIN_SEARCH_LENGTH) {
      return List.of();
    }

    User currentUser = currentUserProvider.getCurrentUser();
    String trimmedQuery = query.trim();

    Page<User> results = userRepository.searchByUsername(
            trimmedQuery,
            currentUser.getId(),
            PageRequest.of(0, SEARCH_LIMIT, Sort.by(Sort.Direction.DESC, "username"))
    );

    return results.stream()
            .map(userPresentationMapper::toSearchResultDto)
            .collect(Collectors.toList());
  }
}
