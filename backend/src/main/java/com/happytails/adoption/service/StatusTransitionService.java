package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.validation.ApplicationStatusTransitionValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusTransitionService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final AnimalRepository animalRepository;
  private final EntityManager entityManager;

  private final ApplicationStatusTransitionValidator validator =
      new ApplicationStatusTransitionValidator();

  public StatusTransitionService(
      ApplicationRepository applicationRepository,
      AnimalRepository animalRepository,
      EntityManager entityManager,
      ApplicationEventPublisher eventPublisher) {

    this.applicationRepository = applicationRepository;
    this.animalRepository = animalRepository;
    this.entityManager = entityManager;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public Application changeStatus(Long applicationId, ApplicationStatus newStatus) {

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Application not found: " + applicationId));

    Long animalId = application.getAnimalId();

    // Lock the animal to serialize status changes for its queue.
    animalRepository
        .findByIdForUpdate(animalId)
        .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));

    // Reload the current state after acquiring the lock.
    entityManager.refresh(application);

    ApplicationStatus oldStatus = application.getStatus();

    validator.validate(oldStatus, newStatus);

    application.setStatus(newStatus);

    Application saved = applicationRepository.save(application);

    eventPublisher.publishEvent(
        new ApplicationStatusChanged(applicationId, animalId, oldStatus, newStatus));

    return saved;
  }
}
