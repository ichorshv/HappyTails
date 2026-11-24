package com.happytails.adoption.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.happytails.adoption.enums.HousingType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The completeness rule: an application is accepted only if the questionnaire and the address are
 * filled in completely. It is enforced by Bean Validation on the request DTOs.
 */
class ApplicationRequestValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  @DisplayName("A completely filled application passes")
  void completeApplicationPasses() {
    assertTrue(
        validator
            .validate(request(7L, answers(4, "Искаме дом."), address("ул. Витоша 10")))
            .isEmpty());
  }

  @Test
  @DisplayName("A missing animal is reported")
  void missingAnimalFails() {
    Set<String> invalid =
        invalidFields(request(null, answers(4, "Искаме дом."), address("ул. Витоша 10")));

    assertEquals(Set.of("animalId"), invalid);
  }

  @Test
  @DisplayName("A missing questionnaire and a missing address are reported")
  void missingSectionsFail() {
    Set<String> invalid = invalidFields(request(7L, null, null));

    assertEquals(Set.of("answers", "address"), invalid);
  }

  @Test
  @DisplayName("Blank motivation and blank street are reported with their field names")
  void blankTextFieldsFail() {
    Set<String> invalid = invalidFields(request(7L, answers(4, "  "), address("")));

    assertEquals(Set.of("answers.motivation", "address.street"), invalid);
  }

  @Test
  @DisplayName("Motivation of exactly 500 characters passes, 501 fails")
  void motivationLengthEdge() {
    assertTrue(
        validator
            .validate(request(7L, answers(4, "а".repeat(500)), address("ул. Витоша 10")))
            .isEmpty());

    Set<String> invalid =
        invalidFields(request(7L, answers(4, "а".repeat(501)), address("ул. Витоша 10")));

    assertEquals(Set.of("answers.motivation"), invalid);
  }

  @Test
  @DisplayName("Hours alone per day must be between 0 and 24")
  void hoursAloneEdges() {
    assertTrue(
        validator
            .validate(request(7L, answers(0, "Искаме дом."), address("ул. Витоша 10")))
            .isEmpty());
    assertTrue(
        validator
            .validate(request(7L, answers(24, "Искаме дом."), address("ул. Витоша 10")))
            .isEmpty());

    assertEquals(
        Set.of("answers.hoursAlonePerDay"),
        invalidFields(request(7L, answers(-1, "Искаме дом."), address("ул. Витоша 10"))));
    assertEquals(
        Set.of("answers.hoursAlonePerDay"),
        invalidFields(request(7L, answers(25, "Искаме дом."), address("ул. Витоша 10"))));
  }

  @Test
  @DisplayName("Every missing questionnaire answer is reported, not only the first")
  void allMissingAnswersAreReported() {
    QuestionnaireAnswers empty =
        new QuestionnaireAnswers(null, null, null, null, null, null, null, null);

    Set<String> invalid = invalidFields(request(7L, empty, address("ул. Витоша 10")));

    assertEquals(
        Set.of(
            "answers.housingType",
            "answers.hasYard",
            "answers.hasChildrenUnder6",
            "answers.hasCats",
            "answers.hasDogs",
            "answers.hasExperience",
            "answers.hoursAlonePerDay",
            "answers.motivation"),
        invalid);
  }

  private Set<String> invalidFields(ApplicationRequest request) {
    return validator.validate(request).stream()
        .map(ConstraintViolation::getPropertyPath)
        .map(Object::toString)
        .collect(Collectors.toSet());
  }

  private ApplicationRequest request(
      Long animalId, QuestionnaireAnswers answers, ApplicationAddress address) {
    return new ApplicationRequest(animalId, answers, address);
  }

  private QuestionnaireAnswers answers(int hoursAlonePerDay, String motivation) {
    return new QuestionnaireAnswers(
        HousingType.HOUSE, true, false, false, true, true, hoursAlonePerDay, motivation);
  }

  private ApplicationAddress address(String street) {
    return new ApplicationAddress(street, "София", "Столична");
  }
}
