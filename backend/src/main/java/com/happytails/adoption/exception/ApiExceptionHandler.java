package com.happytails.adoption.exception;

import com.happytails.adoption.dto.ErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Turns errors into the common error format. Issue #33 extends it for all endpoints. */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
    ErrorResponse body =
        new ErrorResponse(ex.getStatus().value(), ex.getCode(), ex.getMessage(), List.of());
    return ResponseEntity.status(ex.getStatus()).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
    ErrorResponse body =
        new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Някои полета са попълнени грешно.",
            details);
    return ResponseEntity.badRequest().body(body);
  }
}
