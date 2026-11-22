package com.happytails.adoption.validation;

import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rejects a second active application of the same applicant for the same animal. */
@Component
@Order(1)
public class DuplicateApplicationRule implements ValidationRule {

  private final ApplicationRepository applicationRepository;

  public DuplicateApplicationRule(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  @Override
  public ValidationResult validate(Long applicantId, ApplicationRequest request) {
    boolean alreadyApplied =
        applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            applicantId, request.animalId(), ApplicationStatus.ACTIVE);

    if (alreadyApplied) {
      return ValidationResult.fail(
          "APPLICATION_DUPLICATE", "Вече имате активно заявление за това животно.");
    }

    return ValidationResult.pass();
  }
}
