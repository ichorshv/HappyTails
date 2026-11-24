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
class DuplicateApplicationRuleTest {

  @Mock private ApplicationRepository applicationRepository;

  @InjectMocks private DuplicateApplicationRule rule;

  @Test
  void passesWhenApplicantHasNoActiveApplicationForAnimal() {
    when(applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            1L, 7L, ApplicationStatus.ACTIVE))
        .thenReturn(false);

    ValidationResult result = rule.validate(1L, request(7L));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenApplicantAlreadyHasActiveApplicationForAnimal() {
    when(applicationRepository.existsByApplicantIdAndAnimalIdAndStatusIn(
            1L, 7L, ApplicationStatus.ACTIVE))
        .thenReturn(true);

    ValidationResult result = rule.validate(1L, request(7L));

    assertFalse(result.passed());
    assertEquals("APPLICATION_DUPLICATE", result.code());
  }

  private ApplicationRequest request(Long animalId) {
    return new ApplicationRequest(
        animalId,
        new QuestionnaireAnswers(
            HousingType.HOUSE, true, false, false, true, true, 4, "Искаме да дадем дом."),
        new ApplicationAddress("ул. Витоша 10", "София", "Столична"));
  }
}
