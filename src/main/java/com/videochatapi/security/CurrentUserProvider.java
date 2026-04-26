package com.videochatapi.security;

import com.videochatapi.model.User;
import com.videochatapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

  private final UserRepository userRepository;

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user");
    }

    String username = authentication.getName();
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
  }

  public Optional<User> getCurrentUserIfPresent() {
    try {
      return Optional.of(getCurrentUser());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}