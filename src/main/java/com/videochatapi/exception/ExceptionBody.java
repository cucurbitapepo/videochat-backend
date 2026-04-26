package com.videochatapi.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ExceptionBody {
  private String message;
  private Map<String, String> errors;

  public ExceptionBody(String message) {
    this.message = message;
    this.errors = new HashMap<>();
  }
}