package com.happytails.adoption.applications.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.enums.HousingType;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.validation.ValidationResult;
import com.happytails.adoption.validation.ValidationRule;
import org.junit.jupiter.api.Test;

public class ValidationRuleTest {
  private final ValidationRule yardRule =
      (applicantId, request) -> {
        if (request.answers().hasYard()) {
          return ValidationResult.pass();
        }
        return ValidationResult.fail("REQUIREMENT_NOT_MET", "Животното има нужда от двор.");
      };

  @Test
  void rulePassesWhenRequirementIsMet() {
    ValidationResult result = yardRule.validate(1L, requestWithYard(true));

    assertTrue(result.passed());
  }

  @Test
  void ruleFailsWithExplanationWhenRequirementIsNotMet() {
    ValidationResult result = yardRule.validate(1L, requestWithYard(false));

    assertFalse(result.passed());
    assertEquals("REQUIREMENT_NOT_MET", result.code());
    assertEquals("Животното има нужда от двор.", result.message());
  }

  private ApplicationRequest requestWithYard(boolean hasYard) {
    QuestionnaireAnswers answers =
        new QuestionnaireAnswers(
            HousingType.HOUSE,
            hasYard,
            false,
            false,
            false,
            true,
            4,
            "Искам да дам дом на животно.");
    ApplicationAddress address = new ApplicationAddress("ул. Витоша 1", "София", "Столична");
    return new ApplicationRequest(10L, answers, address);
  }
}
