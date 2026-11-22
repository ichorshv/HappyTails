package com.happytails.adoption.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.HousingType;
import com.happytails.adoption.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationLimitRuleTest {

  @Mock private ApplicationRepository applicationRepository;

  @InjectMocks private ApplicationLimitRule rule;

  @Test
  void passesWhenApplicantHasNoActiveApplications() {
    givenActiveApplications(1L, 0);

    assertTrue(rule.validate(1L, request()).passed());
  }

  @Test
  void passesWhenApplicantHasTwoActiveApplications() {
    givenActiveApplications(1L, 2);

    assertTrue(rule.validate(1L, request()).passed());
  }

  @Test
  void failsWhenApplicantAlreadyHasThreeActiveApplications() {
    givenActiveApplications(1L, 3);

    ValidationResult result = rule.validate(1L, request());

    assertFalse(result.passed());
    assertEquals("APPLICATION_LIMIT_REACHED", result.code());
  }

  private void givenActiveApplications(Long applicantId, long count) {
    when(applicationRepository.countByApplicantIdAndStatusIn(applicantId, ApplicationStatus.ACTIVE))
        .thenReturn(count);
  }

  private ApplicationRequest request() {
    return new ApplicationRequest(
        7L,
        new QuestionnaireAnswers(
            HousingType.HOUSE, true, false, false, true, true, 4, "Искаме да дадем дом."),
        new ApplicationAddress("ул. Витоша 10", "София", "Столична"));
  }
}
