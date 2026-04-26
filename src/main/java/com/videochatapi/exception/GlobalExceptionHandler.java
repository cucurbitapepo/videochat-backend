package com.videochatapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final Environment environment;

  @Autowired
  public GlobalExceptionHandler(Environment environment) {
    this.environment = environment;
  }

  private boolean isProduction() {
    String[] activeProfiles = environment.getActiveProfiles();
    return Arrays.stream(activeProfiles)
            .noneMatch(profile -> profile.equals("dev") || profile.equals("test"));
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ExceptionBody> handleAllExceptions(Exception e, WebRequest request) {
    logger.error("Unexpected error: {}", e.getMessage(), e);

    String message = isProduction() ? "Internal server error" : e.getMessage();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ExceptionBody(message, Collections.emptyMap()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ExceptionBody> handleIllegalArgumentException(IllegalArgumentException e) {
    logger.warn("Illegal argument: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ExceptionBody("Illegal Argument", Collections.singletonMap("error", "Illegal Argument")));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<ExceptionBody> handleAccessDeniedException(AccessDeniedException e) {
    logger.warn("Access denied: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ExceptionBody("Access denied", Collections.singletonMap("error", "Access Denied")));
  }

  @ExceptionHandler({
          ContactAlreadyExistsException.class,
          ContactNotFoundException.class,
          InvalidContactOperationException.class,
          CallNotFoundException.class
  })
  public ResponseEntity<ExceptionBody> handleCustomExceptions(RuntimeException e) {
    HttpStatus status;
    String errorType;

    if (e instanceof ContactAlreadyExistsException) {
      status = HttpStatus.CONFLICT;
      errorType = "Contact Already Exists";
    } else if (e instanceof ContactNotFoundException) {
      status = HttpStatus.NOT_FOUND;
      errorType = "Contact Not Found";
    } else if (e instanceof InvalidContactOperationException) {
      status = HttpStatus.BAD_REQUEST;
      errorType = "Invalid Contact Operation";
    } else {
      status = HttpStatus.NOT_FOUND;
      errorType = "Call Not Found";
    }

    logger.info("{}: {}", errorType, e.getMessage());
    return ResponseEntity.status(status)
            .body(new ExceptionBody(e.getMessage(), Collections.singletonMap("error", errorType)));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ExceptionBody> handleValidationExceptions(MethodArgumentNotValidException e) {
    Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                    FieldError::getField,
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
            ));

    logger.info("Validation failed: {}", errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ExceptionBody("Validation failed", errors));
  }

}