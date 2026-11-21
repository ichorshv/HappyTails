package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.EnumSet;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!stub")
public class QueueServiceImpl implements QueueService {

  private static final EnumSet<ApplicationStatus> QUEUE_STATUSES =
      EnumSet.of(
          ApplicationStatus.WAITING, ApplicationStatus.YOUR_TURN, ApplicationStatus.WALK_SCHEDULED);

  private final ApplicationRepository applicationRepository;
  private final StatusTransitionService statusTransitionService;

  public QueueServiceImpl(
      ApplicationRepository applicationRepository,
      StatusTransitionService statusTransitionService) {
    this.applicationRepository = applicationRepository;
    this.statusTransitionService = statusTransitionService;
  }

  @Override
  @Transactional(readOnly = true)
  public int getQueuePosition(Long applicationId) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Application not found: " + applicationId));

    if (!QUEUE_STATUSES.contains(application.getStatus())) {
      return 0;
    }

    List<Application> queue =
        applicationRepository.findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
            application.getAnimalId(), QUEUE_STATUSES);

    for (int i = 0; i < queue.size(); i++) {
      if (queue.get(i).getId().equals(applicationId)) {
        return i + 1;
      }
    }

    return 0;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isFirstInQueue(Long applicationId) {
    return getQueuePosition(applicationId) == 1;
  }

  @Override
  @Transactional
  public void markWalkScheduled(Long applicationId) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () -> new EntityNotFoundException("Application not found: " + applicationId));

    if (application.getStatus() != ApplicationStatus.YOUR_TURN) {
      throw new IllegalStateException("Application is not YOUR_TURN: " + applicationId);
    }

    if (!isFirstInQueue(applicationId)) {
      throw new IllegalStateException("Application is not first in queue: " + applicationId);
    }

    statusTransitionService.changeStatus(applicationId, ApplicationStatus.WALK_SCHEDULED);
  }
}
