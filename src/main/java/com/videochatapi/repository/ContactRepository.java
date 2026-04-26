package com.videochatapi.repository;

import com.videochatapi.model.Contact;
import com.videochatapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

  @Query("SELECT c FROM Contact c WHERE c.owner.id = :ownerId AND c.contact.id = :contactId")
  Optional<Contact> findByOwnerAndContact(@Param("ownerId") Long ownerId, @Param("contactId") Long contactId);

  List<Contact> findByOwner(User owner);

  boolean existsByOwnerAndContact(User owner, User contact);

  Long countByOwner(User owner);

}
