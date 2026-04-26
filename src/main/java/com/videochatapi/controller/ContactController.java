package com.videochatapi.controller;

import com.videochatapi.dto.contact.ContactDto;
import com.videochatapi.dto.contact.ContactRequestDto;
import com.videochatapi.dto.user.UserSearchResultDto;
import com.videochatapi.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;

  @GetMapping
  public ResponseEntity<List<ContactDto>> getContacts() {
    return ResponseEntity.ok(contactService.getContacts());
  }

  @PostMapping
  public ResponseEntity<ContactDto> addContact(@RequestBody ContactRequestDto request) {
    return ResponseEntity.ok(contactService.addContact(request.getUserId()));
  }

  @DeleteMapping("/{contactUserId}")
  public ResponseEntity<Void> removeContact(@PathVariable Long contactUserId) {
    contactService.removeContact(contactUserId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  public ResponseEntity<Page<UserSearchResultDto>> searchUsers(
          @RequestParam String q,
          @PageableDefault(size = 20) Pageable pageable
  ) {
    return ResponseEntity.ok(contactService.searchUsersWithContactStatus(q, pageable));
  }

}
