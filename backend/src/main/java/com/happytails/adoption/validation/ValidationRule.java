package com.happytails.adoption.validation;

import com.happytails.adoption.dto.ApplicationRequest;

public interface ValidationRule {
  ValidationResult validate(Long applicantId, ApplicationRequest request);
}
