package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.dto.ApplicationStatusResponse;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationStatusServiceTest {

  private ApplicationRepository applicationRepository;
  private QueueService queueService;
  private CurrentUserService currentUserService;
  private ApplicationStatusService service;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    queueService = mock(QueueService.class);
    currentUserService = mock(CurrentUserService.class);

    service = new ApplicationStatusService(applicationRepository, queueService, currentUserService);
  }

  private Application createApplication(Long id, Long applicantId, ApplicationStatus status) {

    Application application =
        new Application(
            applicantId, 100L, "Street", "Sofia", "Sofia", Instant.parse("2026-11-01T10:00:00Z"));

    application.setStatus(status);

    try {
      var field = Application.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(application, id);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }

    return application;
  }

  @Test
  void returnsStatusForOwnApplication() {
    Application application = createApplication(15L, 10L, ApplicationStatus.YOUR_TURN);
    Instant deadline = Instant.parse("2026-11-23T18:00:00Z");
    application.setTurnDeadline(deadline);

    when(currentUserService.getCurrentUserId()).thenReturn(10L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
    when(queueService.getQueuePosition(15L)).thenReturn(1);

    ApplicationStatusResponse result = service.getStatus(15L);

    assertEquals(15L, result.applicationId());
    assertEquals(ApplicationStatus.YOUR_TURN, result.status());
    assertEquals(1, result.queuePosition());
    assertEquals(deadline, result.deadline());
  }

  @Test
  void rejectsAccessToAnotherUsersApplication() {
    Application application = createApplication(15L, 20L, ApplicationStatus.WAITING);

    when(currentUserService.getCurrentUserId()).thenReturn(10L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));

    ApiException error = assertThrows(ApiException.class, () -> service.getStatus(15L));

    assertEquals(403, error.getStatus().value());
    assertEquals("APPLICATION_ACCESS_DENIED", error.getCode());

    verifyNoInteractions(queueService);
  }

  @Test
  void returnsNotFoundWhenApplicationDoesNotExist() {
    when(currentUserService.getCurrentUserId()).thenReturn(10L);
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    ApiException error = assertThrows(ApiException.class, () -> service.getStatus(99L));

    assertEquals(404, error.getStatus().value());
    assertEquals("APPLICATION_NOT_FOUND", error.getCode());

    verifyNoInteractions(queueService);
  }

  @Test
  void returnsZeroPositionForWithdrawnApplication() {
    Application application = createApplication(15L, 10L, ApplicationStatus.WITHDRAWN);

    when(currentUserService.getCurrentUserId()).thenReturn(10L);
    when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
    when(queueService.getQueuePosition(15L)).thenReturn(0);

    ApplicationStatusResponse result = service.getStatus(15L);

    assertEquals(ApplicationStatus.WITHDRAWN, result.status());
    assertEquals(0, result.queuePosition());
  }
}
