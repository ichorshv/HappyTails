package com.happytails.adoption.service;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.AnimalRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The only place where an animal's status changes. */
@Service
public class AnimalStatusService {

  private final AnimalRepository animalRepository;
  private final ApplicationEventPublisher eventPublisher;

  public AnimalStatusService(
      AnimalRepository animalRepository, ApplicationEventPublisher eventPublisher) {
    this.animalRepository = animalRepository;
    this.eventPublisher = eventPublisher;
  }

  /** Staff publish a draft: it appears in the catalog. */
  @Transactional
  public void publish(Long animalId) {
    changeStatus(animalId, AnimalStatus.AVAILABLE);
  }

  /** An application is approved. */
  @Transactional
  public void markReserved(Long animalId) {
    changeStatus(animalId, AnimalStatus.RESERVED);
  }

  /** The animal is handed over. */
  @Transactional
  public void markAdopted(Long animalId) {
    changeStatus(animalId, AnimalStatus.ADOPTED);
  }

  /** An approved adoption falls through and the animal is looking for a home again. */
  @Transactional
  public void markAvailable(Long animalId) {
    changeStatus(animalId, AnimalStatus.AVAILABLE);
  }

  private void changeStatus(Long animalId, AnimalStatus newStatus) {
    Animal animal =
        animalRepository
            .findById(animalId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "ANIMAL_NOT_FOUND", "Животното не е намерено."));

    AnimalStatus oldStatus = animal.getStatus();
    if (!oldStatus.canChangeTo(newStatus)) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "ANIMAL_INVALID_STATUS",
          "Статусът на животното не позволява тази промяна.");
    }

    animal.changeStatus(newStatus);
    eventPublisher.publishEvent(new AnimalStatusChanged(animalId, oldStatus, newStatus));
  }
}
