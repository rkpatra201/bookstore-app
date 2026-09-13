package com.bookstore.backend.exceptions;

import com.bookstore.backend.dtos.DataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAccountException.class)
  public ResponseEntity<DataResponse<Void>> handleUserAccountException(UserAccountException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(ex.getHttpCode()).body(response);
  }

  @ExceptionHandler(ItemNotFoundException.class)
  public ResponseEntity<DataResponse<Void>> handleNotFound(ItemNotFoundException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<DataResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(
            false,
            ex.getMessage(),
            null
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<DataResponse<Void>> handleIllegalState(IllegalStateException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(
            false,
            ex.getMessage(),
            null
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<DataResponse<Void>> handleGenericException(Exception ex) {
    log.error("Error while processing request: ", ex);
    DataResponse<Void> response = new DataResponse<>(
            false,
            "An unexpected internal error occurred on the server.",
            null
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}
