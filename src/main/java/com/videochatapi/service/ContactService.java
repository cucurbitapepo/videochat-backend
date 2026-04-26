package com.videochatapi.service;

import com.videochatapi.dto.contact.ContactDto;
import com.videochatapi.dto.user.UserSearchResultDto;
import com.videochatapi.exception.ContactAlreadyExistsException;
import com.videochatapi.exception.ContactNotFoundException;
import com.videochatapi.exception.InvalidContactOperationException;
import com.videochatapi.mapping.UserPresentationMapper;
import com.videochatapi.model.Contact;
import com.videochatapi.model.User;
import com.videochatapi.repository.ContactRepository;
import com.videochatapi.repository.UserRepository;
import com.videochatapi.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

  private static final int MAX_CONTACTS = 500;

  private final ContactRepository contactRepository;
  private final UserRepository userRepository;
  private final CurrentUserProvider currentUserProvider;
  private final UserPresentationMapper userPresentationMapper;

  private static final int MIN_SEARCH_LENGTH = 3;

  public ContactDto toDto(Contact contact) {
    ContactDto dto = new ContactDto();
    dto.setId(contact.getId());
    dto.setAlias(contact.getAlias());
    dto.setCreatedAt(contact.getCreatedAt().toString());
    dto.setUpdatedAt(contact.getUpdatedAt().toString());

    dto.setContact(userPresentationMapper.toDtoWithOnline(contact.getContact()));

    return dto;
  }

  @Transactional
  public ContactDto addContact(Long contactId) {
    User currentUser = currentUserProvider.getCurrentUser();
    User contactUser = userRepository.findById(contactId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    if (currentUser.getId().equals(contactId)) {
      throw new InvalidContactOperationException("Нельзя добавить самого себя в контакты");
    }

    if (contactRepository.existsByOwnerAndContact(currentUser, contactUser)) {
      throw new ContactAlreadyExistsException("Пользователь уже в контактах");
    }

    if (contactRepository.countByOwner(currentUser) >= MAX_CONTACTS) {
      throw new InvalidContactOperationException("Достигнуто максимальное количество контактов");
    }

    Contact contact = new Contact();
    contact.setOwner(currentUser);
    contact.setContact(contactUser);

    Contact savedContact = contactRepository.save(contact);
    return toDto(savedContact);
  }

  @Transactional
  public void removeContact(Long userContactId) {
    User currentUser = currentUserProvider.getCurrentUser();
    User contactUser = userRepository.findById(userContactId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    Contact contact = contactRepository.findByOwnerAndContact(currentUser.getId(), contactUser.getId())
            .orElseThrow(() -> new ContactNotFoundException("Контакт не найден"));

    contactRepository.delete(contact);
  }

  public boolean isContact(Long userId) {
    User currentUser = currentUserProvider.getCurrentUser();
    User targetUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

    return contactRepository.existsByOwnerAndContact(currentUser, targetUser);
  }

  public List<ContactDto> getContacts() {
    User currentUser = currentUserProvider.getCurrentUser();
    return contactRepository.findByOwner(currentUser).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
  }

  public Page<UserSearchResultDto> searchUsersWithContactStatus(String query, Pageable pageable) {

    if (query == null || query.trim().length() < MIN_SEARCH_LENGTH) {
      return Page.empty();
    }

    User currentUser = currentUserProvider.getCurrentUser();
    Page<User> users = userRepository.searchByUsername(query, currentUser.getId(), pageable);

    return users.map(user -> {
      UserSearchResultDto dto = userPresentationMapper.toSearchResultDto(user);
      dto.setContact(contactRepository.existsByOwnerAndContact(currentUser, user));
      return dto;
    });
  }
}