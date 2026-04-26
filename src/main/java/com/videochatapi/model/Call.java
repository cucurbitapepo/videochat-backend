package com.videochatapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "calls")
public class Call {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String callId = UUID.randomUUID().toString();

  @ManyToOne
  @JoinColumn(name = "caller_id", nullable = false)
  private User caller;

  @Enumerated(EnumType.STRING)
  private CallStatus status = CallStatus.WAITING;

  @ManyToMany
  @JoinTable(
          name = "call_participants",
          joinColumns = @JoinColumn(name = "call_id"),
          inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<User> participants = new HashSet<>();

  private LocalDateTime createdAt = LocalDateTime.now();

  private LocalDateTime endedAt;

  public void addParticipant(User user) {
    participants.add(user);
  }

  public boolean isParticipant(User user) {
    return participants.contains(user);
  }
}
