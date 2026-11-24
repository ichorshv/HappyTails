package com.happytails.adoption.service;

import com.happytails.adoption.dto.ApplicationAddress;
import com.happytails.adoption.dto.ApplicationCreatedResponse;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.dto.QuestionnaireAnswers;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.ApplicationAnswers;
import com.happytails.adoption.enums.AnimalStatus;
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
  private final ApplicationQueueEntryService queueEntryService;
  private final CurrentUserService currentUserService;
  private final GeocodingService geocodingService;

  public ApplicationSubmissionService(
      List<ValidationRule> rules,
      AnimalRepository animalRepository,
      ApplicationRepository applicationRepository,
      ApplicationAnswersRepository answersRepository,
      ApplicationQueueEntryService queueEntryService,
      CurrentUserService currentUserService,
      GeocodingService geocodingService) {
    this.rules = rules;
    this.animalRepository = animalRepository;
    this.applicationRepository = applicationRepository;
    this.answersRepository = answersRepository;
    this.queueEntryService = queueEntryService;
    this.currentUserService = currentUserService;
    this.geocodingService = geocodingService;
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
    Coordinates coordinates = locate(address);

    // The coordinates are set before the first save, so they are part of the INSERT.
    Application newApplication =
        new Application(
            applicantId,
            request.animalId(),
            address.street(),
            address.city(),
            address.municipality(),
            Instant.now());
    newApplication.setCoordinates(coordinates.latitude(), coordinates.longitude());
    Application application = applicationRepository.save(newApplication);

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

    // The queue puts the application in line: YOUR_TURN (with a 48-hour deadline) for the first
    // applicant of the animal, WAITING for everyone else.
    Application queued = queueEntryService.onApplicationValidated(application.getId());

    return new ApplicationCreatedResponse(queued.getId(), queued.getStatus());
  }

  /** Finds the coordinates of the address; an address that is not on the map is rejected. */
  private Coordinates locate(ApplicationAddress address) {
    // The shelter works in Bulgaria, so the country is added to avoid matches abroad.
    String searchText = address.street() + ", " + address.city() + ", България";
    return geocodingService
        .geocode(searchText)
        .orElseThrow(
            () ->
                new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "ADDRESS_NOT_FOUND",
                    "Адресът не беше намерен на картата."));
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
