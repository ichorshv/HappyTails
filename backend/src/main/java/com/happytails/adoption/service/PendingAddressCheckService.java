package com.happytails.adoption.service;

import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import com.happytails.adoption.repository.ApplicationRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks the address of an application that was saved as SUBMITTED because the geocoding service
 * was unavailable at the time.
 */
@Service
public class PendingAddressCheckService {

  private static final Logger log = LoggerFactory.getLogger(PendingAddressCheckService.class);

  private final ApplicationRepository applicationRepository;
  private final GeocodingService geocodingService;
  private final ApplicationQueueEntryService queueEntryService;
  private final StatusTransitionService statusTransitionService;

  public PendingAddressCheckService(
      ApplicationRepository applicationRepository,
      GeocodingService geocodingService,
      ApplicationQueueEntryService queueEntryService,
      StatusTransitionService statusTransitionService) {
    this.applicationRepository = applicationRepository;
    this.geocodingService = geocodingService;
    this.queueEntryService = queueEntryService;
    this.statusTransitionService = statusTransitionService;
  }

  /**
   * Looks up the address of one application. If it is found, the coordinates are saved and the
   * application enters the queue. If it is not found, the application is rejected. Applications
   * that are no longer SUBMITTED or already have coordinates are skipped.
   *
   * @param applicationId the application to check
   * @throws GeocodingUnavailableException when the geocoding service is still unavailable; the
   *     application is left unchanged and can be checked again later
   */
  @Transactional
  public void checkAddress(Long applicationId) {
    Application application = applicationRepository.findById(applicationId).orElse(null);
    if (application == null
        || application.getStatus() != ApplicationStatus.SUBMITTED
        || application.getLatitude() != null) {
      return;
    }

    Optional<Coordinates> found =
        geocodingService.geocode(AddressText.of(application.getStreet(), application.getCity()));

    if (found.isEmpty()) {
      log.info("Address of application {} was not found, rejecting it", applicationId);
      application.setRejectionReason("Адресът не беше намерен на картата.");
      statusTransitionService.changeStatus(applicationId, ApplicationStatus.REJECTED);
      return;
    }

    Coordinates coordinates = found.get();
    application.setCoordinates(coordinates.latitude(), coordinates.longitude());
    // The queue reloads the application from the database, so the coordinates are saved first.
    applicationRepository.saveAndFlush(application);
    queueEntryService.onApplicationValidated(applicationId);
  }
}
