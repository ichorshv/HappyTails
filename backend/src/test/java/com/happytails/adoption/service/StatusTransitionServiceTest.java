package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusTransitionServiceTest {

  private ApplicationRepository applicationRepository;
  private StatusTransitionService statusTransitionService;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    statusTransitionService = new StatusTransitionService(applicationRepository);
  }

  @Test
  void allowedTransitionShouldUpdateAndSaveApplication() {
    Application application = createApplication();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    when(applicationRepository.save(application)).thenReturn(application);

    Application result = statusTransitionService.changeStatus(1L, ApplicationStatus.WAITING);

    assertEquals(ApplicationStatus.WAITING, result.getStatus());
    verify(applicationRepository).save(application);
  }

  @Test
  void forbiddenTransitionShouldNotSaveApplication() {
    Application application = createApplication();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    assertThrows(
        IllegalArgumentException.class,
        () -> statusTransitionService.changeStatus(1L, ApplicationStatus.APPROVED));

    assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());

    verify(applicationRepository, never()).save(any(Application.class));
  }

  @Test
  void missingApplicationShouldThrowException() {
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class,
        () -> statusTransitionService.changeStatus(99L, ApplicationStatus.WAITING));

    verify(applicationRepository, never()).save(any(Application.class));
  }

  private Application createApplication() {
    return new Application(
        10L, 5L, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-16T10:00:00Z"));
  }
}
