package com.happytails.adoption.applications.validation;

import com.happytails.adoption.applications.dto.ApplicationRequest;

public interface ValidationRule {
    ValidationResult validate(Long applicantId, ApplicationRequest request);
}
