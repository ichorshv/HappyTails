package com.happytails.adoption.service;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationCreatedResponse;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.ApplicationAnswers;
import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationAnswersRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.validation.ValidationResult;
import com.happytails.adoption.validation.ValidationRule;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationSubmissionService {

  private static final Map<String, HttpStatus> RULE_ERROR_STATUS =
      Map.of(
          "APPLICATION_DUPLICATE", HttpStatus.CONFLICT,
          "APPLICATION_LIMIT_REACHED", HttpStatus.UNPROCESSABLE_ENTITY);

  private final List<ValidationRule> rules;
  private final AnimalRepository animalRepository;
  private final ApplicationRepository applicationRepository;
  private final ApplicationAnswersRepository answersRepository;
  private final StatusTransitionService statusTransitionService;
  private final CurrentUserService currentUserService;

  public ApplicationSubmissionService(
      List<ValidationRule> rules,
      AnimalRepository animalRepository,
      ApplicationRepository applicationRepository,
      ApplicationAnswersRepository answersRepository,
      StatusTransitionService statusTransitionService,
      CurrentUserService currentUserService) {
    this.rules = rules;
    this.animalRepository = animalRepository;
    this.applicationRepository = applicationRepository;
    this.answersRepository = answersRepository;
    this.statusTransitionService = statusTransitionService;
    this.currentUserService = currentUserService;
  }

  @Transactional
  public ApplicationCreatedResponse submit(ApplicationRequest request) {
    Long applicantId = currentUserService.getCurrentUserId();

    requireAvailableAnimal(request.animalId());

    // Rules run in order; the first failed rule stops the submission.
    for (ValidationRule rule : rules) {
      ValidationResult result = rule.validate(applicantId, request);
      if (!result.passed()) {
        HttpStatus status =
            RULE_ERROR_STATUS.getOrDefault(result.code(), HttpStatus.UNPROCESSABLE_ENTITY);
        throw new ApiException(status, result.code(), result.message());
      }
    }

    ApplicationAddress address = request.address();
    Application application =
        applicationRepository.save(
            new Application(
                applicantId,
                request.animalId(),
                address.street(),
                address.city(),
                address.municipality(),
                Instant.now()));

    QuestionnaireAnswers answers = request.answers();
    answersRepository.save(
        new ApplicationAnswers(
            application,
            answers.housingType(),
            answers.hasYard(),
            answers.hasChildrenUnder6(),
            answers.hasCats(),
            answers.hasDogs(),
            answers.hasExperience(),
            answers.hoursAlonePerDay(),
            answers.motivation()));

    // TODO: QueueService will promote the first applicant to YOUR_TURN (agreed with Gosho).
    Application queued =
        statusTransitionService.changeStatus(application.getId(), ApplicationStatus.WAITING);

    return new ApplicationCreatedResponse(queued.getId(), queued.getStatus());
  }

  private void requireAvailableAnimal(Long animalId) {
    boolean available =
        animalRepository
            .findById(animalId)
            .filter(animal -> animal.getStatus() == AnimalStatus.AVAILABLE)
            .isPresent();

    if (!available) {
      throw new ApiException(
          HttpStatus.NOT_FOUND, "ANIMAL_NOT_AVAILABLE", "Животното не е налично за осиновяване.");
    }
  }
}
