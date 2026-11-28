package com.happytails.adoption.service;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class QueueExpirationScheduler {

  private static final Logger log = LoggerFactory.getLogger(QueueExpirationScheduler.class);

  private final ApplicationRepository applicationRepository;
  private final QueueAdvancementService queueAdvancementService;

  public QueueExpirationScheduler(
      ApplicationRepository applicationRepository,
      QueueAdvancementService queueAdvancementService) {
    this.applicationRepository = applicationRepository;
    this.queueAdvancementService = queueAdvancementService;
  }

  @Scheduled(fixedDelay = 60000)
  public void checkExpiredApplications() {

    List<Application> expiredApplications =
        applicationRepository.findByStatusAndTurnDeadlineLessThanEqual(
            ApplicationStatus.YOUR_TURN, Instant.now());

    for (Application application : expiredApplications) {
      Long applicationId = application.getId();

      try {
        boolean expired = queueAdvancementService.expireAndAdvance(applicationId);

        if (expired) {
          log.info("Application {} expired and queue advancement checked", applicationId);
        }

      } catch (Exception exception) {
        log.error("Failed to process expired application {}", applicationId, exception);
      }
    }
  }
}
