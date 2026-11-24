package com.happytails.adoption.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.HousingType;
import com.happytails.adoption.enums.RequirementType;
import com.happytails.adoption.enums.Species;
import com.happytails.adoption.repository.AnimalRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnimalRequirementsRuleTest {

  @Mock private AnimalRepository animalRepository;

  @InjectMocks private AnimalRequirementsRule rule;

  @Test
  void passesWhenAnimalHasNoRequirementsWhateverTheAnswersAre() {
    givenAnimalWithRequirements();

    ValidationResult result = rule.validate(1L, request(worstAnswers()));

    assertTrue(result.passed());
  }

  @Test
  void passesWhenAllRequirementsAreMet() {
    givenAnimalWithRequirements(RequirementType.values());

    ValidationResult result = rule.validate(1L, request(goodAnswers()));

    assertTrue(result.passed());
  }

  @Test
  void passesWhenAnimalIsNotFound() {
    when(animalRepository.findById(7L)).thenReturn(Optional.empty());

    ValidationResult result = rule.validate(1L, request(worstAnswers()));

    assertTrue(result.passed());
  }

  @Test
  void checksOnlyTheRequirementsOfTheAnimal() {
    givenAnimalWithRequirements(RequirementType.YARD);

    // The applicant has cats and dogs and no experience, but the animal asks only for a yard.
    ValidationResult result = rule.validate(1L, request(answers(true, true, true, true, false)));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenYardIsRequiredButApplicantHasNone() {
    givenAnimalWithRequirements(RequirementType.YARD);

    ValidationResult result = rule.validate(1L, request(answers(false, false, false, false, true)));

    assertRejected(result, "Животното има нужда от двор.");
  }

  @Test
  void passesWhenYardIsRequiredAndApplicantHasOne() {
    givenAnimalWithRequirements(RequirementType.YARD);

    ValidationResult result = rule.validate(1L, request(answers(true, false, false, false, true)));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenAnimalCannotLiveWithCatsButApplicantHasCats() {
    givenAnimalWithRequirements(RequirementType.NO_CATS);

    ValidationResult result = rule.validate(1L, request(answers(true, true, false, false, true)));

    assertRejected(result, "Животното не е подходящо за дом с котки.");
  }

  @Test
  void passesWhenAnimalCannotLiveWithCatsAndApplicantHasNone() {
    givenAnimalWithRequirements(RequirementType.NO_CATS);

    ValidationResult result = rule.validate(1L, request(answers(true, false, true, true, true)));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenAnimalCannotLiveWithDogsButApplicantHasDogs() {
    givenAnimalWithRequirements(RequirementType.NO_DOGS);

    ValidationResult result = rule.validate(1L, request(answers(true, false, true, false, true)));

    assertRejected(result, "Животното не е подходящо за дом с кучета.");
  }

  @Test
  void passesWhenAnimalCannotLiveWithDogsAndApplicantHasNone() {
    givenAnimalWithRequirements(RequirementType.NO_DOGS);

    ValidationResult result = rule.validate(1L, request(answers(true, true, false, true, true)));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenAnimalIsNotForSmallChildrenButApplicantHasThem() {
    givenAnimalWithRequirements(RequirementType.NO_SMALL_CHILDREN);

    ValidationResult result = rule.validate(1L, request(answers(true, false, false, true, true)));

    assertRejected(result, "Животното не е подходящо за дом с деца под 6 години.");
  }

  @Test
  void passesWhenAnimalIsNotForSmallChildrenAndApplicantHasNone() {
    givenAnimalWithRequirements(RequirementType.NO_SMALL_CHILDREN);

    ValidationResult result = rule.validate(1L, request(answers(true, true, true, false, true)));

    assertTrue(result.passed());
  }

  @Test
  void failsWhenExperienceIsRequiredButApplicantHasNone() {
    givenAnimalWithRequirements(RequirementType.EXPERIENCE);

    ValidationResult result = rule.validate(1L, request(answers(true, false, false, false, false)));

    assertRejected(result, "Животното има нужда от опитен стопанин.");
  }

  @Test
  void passesWhenExperienceIsRequiredAndApplicantHasIt() {
    givenAnimalWithRequirements(RequirementType.EXPERIENCE);

    ValidationResult result = rule.validate(1L, request(answers(false, true, true, true, true)));

    assertTrue(result.passed());
  }

  @Test
  void reportsOnlyTheFirstUnmetRequirementInFixedOrder() {
    givenAnimalWithRequirements(RequirementType.values());

    ValidationResult result = rule.validate(1L, request(worstAnswers()));

    // YARD comes first in the enum, so it is the one reported.
    assertRejected(result, "Животното има нужда от двор.");
  }

  @Test
  void reportsTheNextUnmetRequirementWhenTheFirstOnesAreMet() {
    givenAnimalWithRequirements(RequirementType.values());

    // Yard is available, but the applicant has cats.
    ValidationResult result = rule.validate(1L, request(answers(true, true, false, false, true)));

    assertRejected(result, "Животното не е подходящо за дом с котки.");
  }

  private void assertRejected(ValidationResult result, String message) {
    assertFalse(result.passed());
    assertEquals("REQUIREMENT_NOT_MET", result.code());
    assertEquals(message, result.message());
  }

  private void givenAnimalWithRequirements(RequirementType... requirements) {
    Animal animal =
        new Animal(
            "Мира",
            Species.DOG,
            25,
            AnimalSize.LARGE,
            "Тестово животно",
            true,
            true,
            null,
            Set.of(requirements),
            Instant.now());
    when(animalRepository.findById(7L)).thenReturn(Optional.of(animal));
  }

  /** The applicant meets every possible requirement. */
  private QuestionnaireAnswers goodAnswers() {
    return answers(true, false, false, false, true);
  }

  /** The applicant breaks every possible requirement. */
  private QuestionnaireAnswers worstAnswers() {
    return answers(false, true, true, true, false);
  }

  private QuestionnaireAnswers answers(
      boolean hasYard,
      boolean hasCats,
      boolean hasDogs,
      boolean hasChildrenUnder6,
      boolean hasExperience) {
    return new QuestionnaireAnswers(
        HousingType.HOUSE,
        hasYard,
        hasChildrenUnder6,
        hasCats,
        hasDogs,
        hasExperience,
        4,
        "Искаме да дадем дом.");
  }

  private ApplicationRequest request(QuestionnaireAnswers answers) {
    return new ApplicationRequest(
        7L, answers, new ApplicationAddress("ул. Витоша 10", "София", "Столична"));
  }
}
