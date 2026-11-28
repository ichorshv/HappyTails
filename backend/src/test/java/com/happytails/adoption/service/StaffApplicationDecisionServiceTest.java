package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StaffApplicationDecisionServiceTest {

  private ApplicationRepository applicationRepository;
  private StatusTransitionService statusTransitionService;
  private AnimalStatusService animalStatusService;

  private StaffApplicationDecisionService service;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    statusTransitionService = mock(StatusTransitionService.class);
    animalStatusService = mock(AnimalStatusService.class);

    service =
        new StaffApplicationDecisionService(
            applicationRepository, statusTransitionService, animalStatusService);
  }

  @Test
  void approvingApplicationReservesAnimal() {
    Application application = createApplication();
    application.setStatus(ApplicationStatus.APPROVED);

    when(statusTransitionService.changeStatus(1L, ApplicationStatus.APPROVED))
        .thenReturn(application);

    Application result = service.approve(1L);

    assertSame(application, result);

    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.APPROVED);

    verify(animalStatusService).markReserved(5L);
  }

  @Test
  void rejectingApplicationSavesReason() {
    Application application = createApplication();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    when(statusTransitionService.changeStatus(1L, ApplicationStatus.REJECTED))
        .thenReturn(application);

    Application result = service.reject(1L, "Applicant does not meet requirements");

    assertSame(application, result);
    assertEquals("Applicant does not meet requirements", application.getRejectionReason());

    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.REJECTED);

    verifyNoInteractions(animalStatusService);
  }

  @Test
  void completingApplicationMarksAnimalAdopted() {
    Application application = createApplication();
    application.setStatus(ApplicationStatus.COMPLETED);

    when(statusTransitionService.changeStatus(1L, ApplicationStatus.COMPLETED))
        .thenReturn(application);

    Application result = service.complete(1L);

    assertSame(application, result);
    verify(animalStatusService).markAdopted(5L);
  }

  @Test
  void withdrawingApprovedApplicationMakesAnimalAvailable() {
    Application application = createApplication();
    application.setStatus(ApplicationStatus.APPROVED);

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    when(statusTransitionService.changeStatus(1L, ApplicationStatus.WITHDRAWN))
        .thenAnswer(
            invocation -> {
              application.setStatus(ApplicationStatus.WITHDRAWN);
              return application;
            });

    Application result = service.withdraw(1L);

    assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());
    verify(animalStatusService).markAvailable(5L);
  }

  @Test
  void withdrawingWaitingApplicationDoesNotChangeAnimalStatus() {
    Application application = createApplication();
    application.setStatus(ApplicationStatus.WAITING);

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    when(statusTransitionService.changeStatus(1L, ApplicationStatus.WITHDRAWN))
        .thenAnswer(
            invocation -> {
              application.setStatus(ApplicationStatus.WITHDRAWN);
              return application;
            });

    Application result = service.withdraw(1L);

    assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());
    verifyNoInteractions(animalStatusService);
  }

  @Test
  void invalidApprovalDoesNotReserveAnimal() {
    when(statusTransitionService.changeStatus(1L, ApplicationStatus.APPROVED))
        .thenThrow(new IllegalArgumentException("Invalid application status transition"));

    assertThrows(IllegalArgumentException.class, () -> service.approve(1L));

    verifyNoInteractions(animalStatusService);
  }

  @Test
  void invalidCompletionDoesNotMarkAnimalAdopted() {
    when(statusTransitionService.changeStatus(1L, ApplicationStatus.COMPLETED))
        .thenThrow(new IllegalArgumentException("Invalid application status transition"));

    assertThrows(IllegalArgumentException.class, () -> service.complete(1L));

    verifyNoInteractions(animalStatusService);
  }

  private Application createApplication() {
    return new Application(
        10L, 5L, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));
  }
}
