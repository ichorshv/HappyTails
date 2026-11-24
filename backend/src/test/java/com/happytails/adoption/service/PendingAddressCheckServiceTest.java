package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PendingAddressCheckServiceTest {

  @Mock private ApplicationRepository applicationRepository;
  @Mock private GeocodingService geocodingService;
  @Mock private ApplicationQueueEntryService queueEntryService;
  @Mock private StatusTransitionService statusTransitionService;

  private PendingAddressCheckService service;

  @BeforeEach
  void setUp() {
    service =
        new PendingAddressCheckService(
            applicationRepository, geocodingService, queueEntryService, statusTransitionService);
  }

  @Test
  @DisplayName("A found address is saved with its coordinates and the application enters the queue")
  void foundAddressEntersTheQueue() {
    Application application = submittedApplication(15L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
    when(geocodingService.geocode("ул. Витоша 10, София, България"))
        .thenReturn(Optional.of(new Coordinates(42.6977, 23.3219)));

    service.checkAddress(15L);

    assertEquals(42.6977, application.getLatitude());
    assertEquals(23.3219, application.getLongitude());
    InOrder inOrder = inOrder(applicationRepository, queueEntryService);
    inOrder.verify(applicationRepository).saveAndFlush(application);
    inOrder.verify(queueEntryService).onApplicationValidated(15L);
  }

  @Test
  @DisplayName("An address that is not found rejects the application")
  void unknownAddressRejectsTheApplication() {
    Application application = submittedApplication(15L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
    when(geocodingService.geocode(anyString())).thenReturn(Optional.empty());

    service.checkAddress(15L);

    assertEquals("Адресът не беше намерен на картата.", application.getRejectionReason());
    verify(statusTransitionService).changeStatus(15L, ApplicationStatus.REJECTED);
    verifyNoInteractions(queueEntryService);
  }

  @Test
  @DisplayName("When geocoding is still unavailable nothing changes and the error is passed on")
  void unavailableGeocodingChangesNothing() {
    Application application = submittedApplication(15L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
    when(geocodingService.geocode(anyString()))
        .thenThrow(new GeocodingUnavailableException("down", new RuntimeException()));

    assertThrows(GeocodingUnavailableException.class, () -> service.checkAddress(15L));

    verify(applicationRepository, never()).saveAndFlush(any());
    verifyNoInteractions(queueEntryService, statusTransitionService);
  }

  @Test
  @DisplayName("An application that is no longer SUBMITTED is skipped")
  void skipsApplicationThatIsNotSubmitted() {
    Application application = submittedApplication(15L);
    application.setStatus(ApplicationStatus.WITHDRAWN);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));

    service.checkAddress(15L);

    verifyNoInteractions(geocodingService, queueEntryService, statusTransitionService);
  }

  @Test
  @DisplayName("An application that already has coordinates is skipped")
  void skipsApplicationWithCoordinates() {
    Application application = submittedApplication(15L);
    application.setCoordinates(42.0, 23.0);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));

    service.checkAddress(15L);

    verifyNoInteractions(geocodingService, queueEntryService, statusTransitionService);
  }

  @Test
  @DisplayName("An unknown application id is skipped")
  void skipsUnknownApplication() {
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    service.checkAddress(99L);

    verifyNoInteractions(geocodingService, queueEntryService, statusTransitionService);
  }

  private Application submittedApplication(Long id) {
    Application application =
        new Application(1L, 7L, "ул. Витоша 10", "София", "Столична", Instant.now());
    ReflectionTestUtils.setField(application, "id", id);
    return application;
  }
}
