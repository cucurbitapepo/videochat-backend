package com.videochatapi.security;

import com.videochatapi.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;

  private final UserDetailsService userDetailsService;
  private SecretKey key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Long userId, String username, Set<UserRole> roles) {
    Claims claims = Jwts.claims()
            .subject(username)
            .add("userId", userId.toString())
            .add("roles", resolveRoles(roles))
            .build();
    Instant validity = Instant.now()
            .plus(jwtProperties.getExpiration(), ChronoUnit.HOURS);

    return Jwts.builder()
            .claims(claims)
            .expiration(Date.from(validity))
            .signWith(key)
            .compact();
  }

  private List<String> resolveRoles(Set<UserRole> roles) {
    return roles.stream()
            .map(Enum::name)
            .collect(Collectors.toList());
  }

  public boolean validateToken(String token) {
    Jws<Claims> claims = Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token);
    return !claims.getPayload().getExpiration().before(new Date());
  }

  public <T> T extractUserId(String token, Class<T> idType) {
    Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    Object userId = claims.get("userId");
    if (userId == null) {
      throw new IllegalArgumentException("Token does not contain userId");
    }

    if (idType.isInstance(userId)) {
      return idType.cast(userId);
    }

    if (idType == Long.class) {
      if (userId instanceof Number) {
        return idType.cast(((Number) userId).longValue());
      }
      if (userId instanceof String) {
        try {
          return idType.cast(Long.parseLong((String) userId));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Cannot parse userId as Long: " + userId, e);
        }
      }
    } else if (idType == String.class) {
      return idType.cast(userId.toString());
    } else if (idType == UUID.class && userId instanceof String) {
      return idType.cast(UUID.fromString((String) userId));
    }

    throw new IllegalArgumentException("Cannot convert userId to " + idType.getSimpleName() +
                                       ", actual type: " + (userId != null ? userId.getClass().getName() : "null"));
  }

  public String extractUsername(String token) {
    return Jwts
            .parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("sub")
            .toString();
  }

  public Authentication getAuthentication(String token) {
    String username = extractUsername(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

}
