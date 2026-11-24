package com.happytails.adoption.validation;

import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.RequirementType;
import com.happytails.adoption.repository.AnimalRepository;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rejects an application when an answer of the questionnaire conflicts with a requirement of the
 * animal. Each requirement is checked with exactly one answer (see docs/api/applications.md).
 */
@Component
@Order(3)
public class AnimalRequirementsRule implements ValidationRule {

  private static final String CODE = "REQUIREMENT_NOT_MET";

  private final AnimalRepository animalRepository;

  public AnimalRequirementsRule(AnimalRepository animalRepository) {
    this.animalRepository = animalRepository;
  }

  @Override
  public ValidationResult validate(Long applicantId, ApplicationRequest request) {
    // A missing animal has nothing to check; the submission service reports it as not available.
    Set<RequirementType> requirements =
        animalRepository.findById(request.animalId()).map(Animal::getRequirements).orElse(Set.of());
    QuestionnaireAnswers answers = request.answers();

    // The enum order is fixed, so the same application always gets the same message.
    for (RequirementType requirement : RequirementType.values()) {
      if (requirements.contains(requirement) && !isMet(requirement, answers)) {
        return ValidationResult.fail(CODE, messageFor(requirement));
      }
    }

    return ValidationResult.pass();
  }

  private boolean isMet(RequirementType requirement, QuestionnaireAnswers answers) {
    return switch (requirement) {
      case YARD -> answers.hasYard();
      case NO_CATS -> !answers.hasCats();
      case NO_DOGS -> !answers.hasDogs();
      case NO_SMALL_CHILDREN -> !answers.hasChildrenUnder6();
      case EXPERIENCE -> answers.hasExperience();
    };
  }

  private String messageFor(RequirementType requirement) {
    return switch (requirement) {
      case YARD -> "Животното има нужда от двор.";
      case NO_CATS -> "Животното не е подходящо за дом с котки.";
      case NO_DOGS -> "Животното не е подходящо за дом с кучета.";
      case NO_SMALL_CHILDREN -> "Животното не е подходящо за дом с деца под 6 години.";
      case EXPERIENCE -> "Животното има нужда от опитен стопанин.";
    };
  }
}
