package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.validation.ApplicationStatusTransitionValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusTransitionService {

  private final ApplicationRepository applicationRepository;
  private final ApplicationStatusTransitionValidator validator =
      new ApplicationStatusTransitionValidator();

  public StatusTransitionService(ApplicationRepository applicationRepository) {
    this.applicationRepository = applicationRepository;
  }

  @Transactional
  public Application changeStatus(Long applicationId, ApplicationStatus newStatus) {

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "APPLICATION_NOT_FOUND",
                        "Application not found: " + applicationId));

    try {
      validator.validate(application.getStatus(), newStatus);
    } catch (IllegalArgumentException ex) {
      throw new ApiException(
          HttpStatus.CONFLICT, "APPLICATION_INVALID_STATUS_TRANSITION", ex.getMessage());
    }

    application.setStatus(newStatus);

    return applicationRepository.save(application);
  }
}
