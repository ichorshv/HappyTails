package com.happytails.adoption.exception;

import com.happytails.adoption.dto.ErrorResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Turns every error from the controllers into the common format {status, code, message, details},
 * so the client can always rely on the code. 401 and 403 from the security filter are written by
 * config/JsonErrorHandlers in the same format.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
    return error(ex.getStatus(), ex.getCode(), ex.getMessage(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();
    return error(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Някои полета са попълнени грешно.", details);
  }

  /** The body is not valid JSON or a value has the wrong type. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
    return error(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Заявката е в грешен формат.", List.of());
  }

  /** A path or query parameter has the wrong type, for example /api/applications/abc/status. */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return error(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Някои полета са попълнени грешно.",
        List.of(ex.getName() + ": грешна стойност"));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
    return error(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Някои полета са попълнени грешно.",
        List.of(ex.getParameterName() + ": задължително поле"));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "Адресът не съществува.", List.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return error(
        HttpStatus.METHOD_NOT_ALLOWED,
        "METHOD_NOT_ALLOWED",
        "Това действие не се поддържа на този адрес.",
        List.of());
  }

  /** Access checks inside a controller or service (for example @PreAuthorize). */
  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
    return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Нямате достъп до тази страница.", List.of());
  }

  /** Two changes to the same record at the same time; the second one is rejected (version). */
  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
    return error(
        HttpStatus.CONFLICT,
        "CONFLICT",
        "Данните бяха променени междувременно. Опитайте отново.",
        List.of());
  }

  /**
   * A database constraint rejected the change, for example two bookings of the same slot. Areas
   * that need their own code (WALK_SLOT_TAKEN) catch it in the service and throw ApiException.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Database constraint violation: {}", ex.getMostSpecificCause().getMessage());
    return error(
        HttpStatus.CONFLICT,
        "CONFLICT",
        "Записът противоречи на вече съществуващи данни.",
        List.of());
  }

  /** Anything unexpected: logged on the server, without internal details for the client. */
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    // Other Spring errors (for example 415 wrong Content-Type) already carry their HTTP status.
    if (ex instanceof org.springframework.web.ErrorResponse springError
        && springError.getStatusCode().is4xxClientError()) {
      HttpStatus status = HttpStatus.valueOf(springError.getStatusCode().value());
      return error(status, "BAD_REQUEST", "Заявката не може да бъде обработена.", List.of());
    }
    log.error("Unexpected error", ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Възникна неочаквана грешка. Опитайте отново по-късно.",
        List.of());
  }

  private static ResponseEntity<ErrorResponse> error(
      HttpStatus status, String code, String message, List<String> details) {
    return ResponseEntity.status(status)
        .body(new ErrorResponse(status.value(), code, message, details));
  }
}
