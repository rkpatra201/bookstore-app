package com.bookstore.backend.exceptions;

import com.bookstore.backend.dtos.DataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserAccountException.class)
  public ResponseEntity<DataResponse<Void>> handleUserAccountException(UserAccountException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(ex.getHttpCode()).body(response);
  }

  @ExceptionHandler(OrderException.class)
  public ResponseEntity<DataResponse<Void>> handleOrderException(OrderException ex) {
    log.error("Error while processing order: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(ex.getHttpCode()).body(response);
  }

  @ExceptionHandler(CartException.class)
  public ResponseEntity<DataResponse<Void>> handleCartException(CartException ex) {
    log.error("Error while processing cart: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(ex.getHttpCode()).body(response);
  }

  @ExceptionHandler(AddressException.class)
  public ResponseEntity<DataResponse<Void>> handleAddressException(AddressException ex) {
    log.error("Error while processing address: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(ex.getHttpCode()).body(response);
  }

  @ExceptionHandler(ItemNotFoundException.class)
  public ResponseEntity<DataResponse<Void>> handleNotFound(ItemNotFoundException ex) {
    log.error("Error while processing request: ", ex);

    DataResponse<Void> response = new DataResponse<>(false, ex.getMessage(), null);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<DataResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
    log.error("Validation error: ", ex);

    String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

    DataResponse<Void> response = new DataResponse<>(
            false,
            "Validation failed: " + errorMessage,
            null
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
