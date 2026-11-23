package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationQueueEntryService {

  private final ApplicationRepository applicationRepository;
  private final AnimalRepository animalRepository;
  private final StatusTransitionService statusTransitionService;
  private final EntityManager entityManager;

  public ApplicationQueueEntryService(
      ApplicationRepository applicationRepository,
      AnimalRepository animalRepository,
      StatusTransitionService statusTransitionService,
      EntityManager entityManager) {

    this.applicationRepository = applicationRepository;
    this.animalRepository = animalRepository;
    this.statusTransitionService = statusTransitionService;
    this.entityManager = entityManager;
  }

  @Transactional
  public Application onApplicationValidated(Long applicationId) {

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Application not found: " + applicationId));

    // Lock the animal to serialize queue updates.

    Long animalId = application.getAnimalId();

    animalRepository
        .findByIdForUpdate(animalId)
        .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));

    // Reload the application's current state after acquiring the lock.
    entityManager.refresh(application);

    if (application.getStatus() != ApplicationStatus.SUBMITTED) {
      throw new IllegalStateException("Application is not SUBMITTED: " + applicationId);
    }

    List<Application> applications =
        applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(application.getAnimalId());

    boolean hasEarlierCandidate = false;
    boolean anotherCandidateIsActive = false;

    for (Application other : applications) {
      if (other.getId().equals(applicationId)) {
        continue;
      }

      ApplicationStatus status = other.getStatus();

      if (status == ApplicationStatus.YOUR_TURN
          || status == ApplicationStatus.WALK_SCHEDULED
          || status == ApplicationStatus.APPROVED) {
        anotherCandidateIsActive = true;
      }

      boolean isEarlier =
          other.getSubmittedAt().isBefore(application.getSubmittedAt())
              || (other.getSubmittedAt().equals(application.getSubmittedAt())
                  && other.getId() < applicationId);

      if (isEarlier
          && (status == ApplicationStatus.SUBMITTED || status == ApplicationStatus.WAITING)) {
        hasEarlierCandidate = true;
      }
    }

    if (hasEarlierCandidate || anotherCandidateIsActive) {
      return statusTransitionService.changeStatus(applicationId, ApplicationStatus.WAITING);
    }

    Application updated =
        statusTransitionService.changeStatus(applicationId, ApplicationStatus.YOUR_TURN);

    updated.setTurnDeadline(Instant.now().plus(48, ChronoUnit.HOURS));

    return applicationRepository.save(updated);
  }
}
