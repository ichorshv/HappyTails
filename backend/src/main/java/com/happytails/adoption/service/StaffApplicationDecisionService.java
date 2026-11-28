package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffApplicationDecisionService {

  private final ApplicationRepository applicationRepository;
  private final StatusTransitionService statusTransitionService;
  private final AnimalStatusService animalStatusService;

  public StaffApplicationDecisionService(
      ApplicationRepository applicationRepository,
      StatusTransitionService statusTransitionService,
      AnimalStatusService animalStatusService) {
    this.applicationRepository = applicationRepository;
    this.statusTransitionService = statusTransitionService;
    this.animalStatusService = animalStatusService;
  }

  @Transactional
  public Application approve(Long applicationId) {
    Application application =
        statusTransitionService.changeStatus(applicationId, ApplicationStatus.APPROVED);

    animalStatusService.markReserved(application.getAnimalId());

    return application;
  }

  @Transactional
  public Application reject(Long applicationId, String reason) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "APPLICATION_NOT_FOUND",
                        "Application not found: " + applicationId));

    application.setRejectionReason(reason);

    return statusTransitionService.changeStatus(applicationId, ApplicationStatus.REJECTED);
  }

  @Transactional
  public Application complete(Long applicationId) {
    Application application =
        statusTransitionService.changeStatus(applicationId, ApplicationStatus.COMPLETED);

    animalStatusService.markAdopted(application.getAnimalId());

    return application;
  }

  @Transactional
  public Application withdraw(Long applicationId) {

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "APPLICATION_NOT_FOUND",
                        "Application not found: " + applicationId));

    ApplicationStatus previousStatus = application.getStatus();

    Application updated =
        statusTransitionService.changeStatus(applicationId, ApplicationStatus.WITHDRAWN);

    if (previousStatus == ApplicationStatus.APPROVED) {
      animalStatusService.markAvailable(updated.getAnimalId());
    }

    return updated;
  }
}
