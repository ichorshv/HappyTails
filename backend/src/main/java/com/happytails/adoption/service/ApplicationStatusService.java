package com.happytails.adoption.service;

import com.happytails.adoption.dto.ApplicationStatusResponse;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationStatusService {

  private final ApplicationRepository applicationRepository;
  private final QueueService queueService;
  private final CurrentUserService currentUserService;

  public ApplicationStatusService(
      ApplicationRepository applicationRepository,
      QueueService queueService,
      CurrentUserService currentUserService) {
    this.applicationRepository = applicationRepository;
    this.queueService = queueService;
    this.currentUserService = currentUserService;
  }

  @Transactional(readOnly = true)
  public ApplicationStatusResponse getStatus(Long applicationId) {

    Long currentUserId = currentUserService.getCurrentUserId();

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "APPLICATION_NOT_FOUND",
                        "Заявлението не е намерено."));

    if (!application.getApplicantId().equals(currentUserId)) {
      throw new ApiException(
          HttpStatus.FORBIDDEN,
          "APPLICATION_ACCESS_DENIED",
          "Нямате право да разглеждате това заявление.");
    }

    int queuePosition = queueService.getQueuePosition(applicationId);

    return new ApplicationStatusResponse(
        application.getId(), application.getStatus(), queuePosition, application.getTurnDeadline());
  }
}
