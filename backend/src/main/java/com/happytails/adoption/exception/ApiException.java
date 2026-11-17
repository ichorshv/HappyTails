package com.happytails.adoption.exception;

import org.springframework.http.HttpStatus;

/** An error with an HTTP status and a code from docs/api, shown to the user in Bulgarian. */
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final String code;

  public ApiException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }
}
