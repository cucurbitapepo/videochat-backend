package com.videochatapi.repository;

import com.videochatapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
  List<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

  Optional<User> findByUsername(String username);

  @Query(value = "SELECT u FROM User u " +
                 "WHERE u.username ILIKE CONCAT('%', :query, '%') " +
                 "AND u.id != :currentUserId " +
                 "ORDER BY videochat.SIMILARITY(LOWER(u.username), LOWER(:query)) DESC",
          countQuery = "SELECT COUNT(u) FROM User u " +
                       "WHERE u.username ILIKE CONCAT('%', :query, '%') " +
                       "AND u.id != :currentUserId")
  Page<User> searchByUsername(
          @Param("query") String query,
          @Param("currentUserId") Long currentUserId,
          Pageable pageable
  );

  @Query(value = "SELECT * FROM videochat.users u " +
                 "WHERE u.username ILIKE '%' || :query || '%' " +
                 "AND u.id != :currentUserId " +
                 "ORDER BY videochat.SIMILARITY(LOWER(u.username), LOWER(:query)) DESC " +
                 "LIMIT 20",
          nativeQuery = true)
  List<User> searchSimilarUsers(
          @Param("query") String query,
          @Param("currentUserId") Long currentUserId
  );

  Optional<User> findById(Long id);

  boolean existsByUsername(String username);

  @Modifying
  @Query("UPDATE User u SET u.fcmToken = :token, u.fcmTokenExpiry = :expiry WHERE u.id = :userId")
  void updateFcmToken(@Param("userId") Long userId,
                      @Param("token") String token,
                      @Param("expiry") LocalDateTime expiry);

  Optional<User> findByFcmToken(String fcmToken);
}
