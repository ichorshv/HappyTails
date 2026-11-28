package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QueueAdvancementService {

  private static final Duration TURN_DURATION = Duration.ofHours(48);

  private final ApplicationRepository applicationRepository;
  private final AnimalRepository animalRepository;
  private final StatusTransitionService statusTransitionService;
  private final Clock clock;
  private final EntityManager entityManager;

  public QueueAdvancementService(
      ApplicationRepository applicationRepository,
      AnimalRepository animalRepository,
      EntityManager entityManager,
      StatusTransitionService statusTransitionService) {
    this.applicationRepository = applicationRepository;
    this.animalRepository = animalRepository;
    this.statusTransitionService = statusTransitionService;
    this.entityManager = entityManager;
    this.clock = Clock.systemUTC();
  }

  @Transactional
  public boolean expireAndAdvance(Long applicationId) {

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Application not found: " + applicationId));

    Long animalId = application.getAnimalId();

    // Serialize all queue changes for this animal.
    animalRepository
        .findByIdForUpdate(animalId)
        .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));

    // Reload the application after acquiring the lock.
    entityManager.refresh(application);

    Instant now = clock.instant();

    if (application.getStatus() != ApplicationStatus.YOUR_TURN
        || application.getTurnDeadline() == null
        || application.getTurnDeadline().isAfter(now)) {
      return false;
    }

    statusTransitionService.changeStatus(applicationId, ApplicationStatus.EXPIRED);

    application.setTurnDeadline(null);

    advanceQueueLocked(animalId);

    return true;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean advanceQueue(Long animalId) {

    animalRepository
        .findByIdForUpdate(animalId)
        .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));

    return advanceQueueLocked(animalId);
  }

  private boolean advanceQueueLocked(Long animalId) {

    List<Application> applications =
        applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(animalId);

    boolean hasActiveCandidate =
        applications.stream()
            .anyMatch(
                application ->
                    application.getStatus() == ApplicationStatus.YOUR_TURN
                        || application.getStatus() == ApplicationStatus.WALK_SCHEDULED
                        || application.getStatus() == ApplicationStatus.APPROVED);

    if (hasActiveCandidate) {
      return false;
    }

    Application firstPending =
        applications.stream()
            .filter(
                application ->
                    application.getStatus() == ApplicationStatus.SUBMITTED
                        || application.getStatus() == ApplicationStatus.WAITING)
            .findFirst()
            .orElse(null);

    if (firstPending == null || firstPending.getStatus() == ApplicationStatus.SUBMITTED) {
      return false;
    }

    for (Application candidate : applications) {
      if (candidate.getStatus() == ApplicationStatus.WAITING) {

        Application next =
            statusTransitionService.changeStatus(candidate.getId(), ApplicationStatus.YOUR_TURN);

        next.setTurnDeadline(clock.instant().plus(TURN_DURATION));
        applicationRepository.save(next);

        return true;
      }
    }

    return false;
  }
}
