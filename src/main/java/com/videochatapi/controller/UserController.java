package com.videochatapi.controller;

import com.videochatapi.dto.user.UserSearchResultDto;
import com.videochatapi.service.ContactService;
import com.videochatapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final ContactService contactService;

  @GetMapping("/search")
  public ResponseEntity<Page<UserSearchResultDto>> searchUsers(
    @RequestParam String q,
    @PageableDefault(size = 20) Pageable pageable
  ) {
    return ResponseEntity.ok(contactService.searchUsersWithContactStatus(q, pageable));
  }
}
