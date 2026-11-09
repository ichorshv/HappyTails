package com.happytails.adoption.applications.validation;

public record ValidationResult(boolean passed, String code, String message) {

  public static ValidationResult pass() {
    return new ValidationResult(true, null, null);
  }

  public static ValidationResult fail(String code, String message) {
    return new ValidationResult(false, code, message);
  }
}
