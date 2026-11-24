package com.happytails.adoption.scheduler;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.service.PendingAddressCheckService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically checks again the address of applications that stayed SUBMITTED because the geocoding
 * service was unavailable when they were submitted.
 */
@Component
public class PendingAddressCheckJob {

  private static final Logger log = LoggerFactory.getLogger(PendingAddressCheckJob.class);

  /** How many applications are checked in one run (geocoding is limited to one request/second). */
  static final int BATCH_SIZE = 10;

  private final ApplicationRepository applicationRepository;
  private final PendingAddressCheckService checkService;

  public PendingAddressCheckJob(
      ApplicationRepository applicationRepository, PendingAddressCheckService checkService) {
    this.applicationRepository = applicationRepository;
    this.checkService = checkService;
  }

  /** Runs every few minutes; the oldest applications are checked first. */
  @Scheduled(
      fixedDelayString = "${applications.address-check.interval:PT5M}",
      initialDelayString = "PT1M")
  public void run() {
    List<Application> pending =
        applicationRepository.findByStatusAndLatitudeIsNullOrderBySubmittedAtAscIdAsc(
            ApplicationStatus.SUBMITTED, Limit.of(BATCH_SIZE));

    for (Application application : pending) {
      try {
        checkService.checkAddress(application.getId());
      } catch (GeocodingUnavailableException e) {
        // The service is down, so the remaining applications would fail too.
        log.warn("Geocoding is still unavailable, the next run will try again");
        return;
      } catch (RuntimeException e) {
        log.error("Address check failed for application {}", application.getId(), e);
      }
    }
  }
}
