package com.happytails.adoption.validation;

import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import org.springframework.stereotype.Component;

/** Rejects a new application when the applicant already has the maximum number of active ones. */
@Component
public class ApplicationLimitRule implements ValidationRule {

  private static final int MAX_ACTIVE_APPLICATIONS = 3;

  private final ApplicationRepository applicationRepository;

  public ApplicationLimitRule(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  @Override
  public ValidationResult validate(Long applicantId, ApplicationRequest request) {
    long activeApplications =
        applicationRepository.countByApplicantIdAndStatusIn(applicantId, ApplicationStatus.ACTIVE);

    if (activeApplications >= MAX_ACTIVE_APPLICATIONS) {
      return ValidationResult.fail(
          "APPLICATION_LIMIT_REACHED", "Можете да имате най-много 3 активни заявления.");
    }

    return ValidationResult.pass();
  }
}
