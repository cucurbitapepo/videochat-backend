package com.videochatapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(name = "role")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
          name = "users_roles",
          schema = "videochat",
          joinColumns = @JoinColumn(name = "user_id")
  )
  @Enumerated(EnumType.STRING)
  Set<UserRole> roles;

  @Column(name = "fcm_token")
  private String fcmToken;

  @Column(name = "fcm_token_expiry")
  private LocalDateTime fcmTokenExpiry;

  @Column(nullable = false)
  private boolean enabled = true;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

}
