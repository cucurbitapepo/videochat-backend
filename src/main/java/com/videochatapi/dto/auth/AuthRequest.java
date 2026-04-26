package com.videochatapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRequest {
  @NotBlank(message = "Имя пользователя не может быть пустым.")
  @Size(min = 3, max = 50, message = "Длина имени пользователя должна быть от 3 до 50 символов.")
  private String username;

  @NotBlank(message = "Пароль не может быть пустым.")
  @Size(min = 8, message = "Длина пароля должна быть не меньше 8 символов.")
  private String password;
}
