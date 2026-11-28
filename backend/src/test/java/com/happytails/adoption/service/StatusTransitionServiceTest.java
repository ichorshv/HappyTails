package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class StatusTransitionServiceTest {

  private ApplicationRepository applicationRepository;
  private StatusTransitionService statusTransitionService;
  private ApplicationEventPublisher eventPublisher;
  private AnimalRepository animalRepository;
  private EntityManager entityManager;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    animalRepository = mock(AnimalRepository.class);
    entityManager = mock(EntityManager.class);
    eventPublisher = mock(ApplicationEventPublisher.class);

    when(animalRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(mock(Animal.class)));

    statusTransitionService =
        new StatusTransitionService(
            applicationRepository, animalRepository, entityManager, eventPublisher);
  }

  @Test
  void allowedTransitionShouldUpdateAndSaveApplication() {
    Application application = createApplication();

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

    when(applicationRepository.save(application)).thenReturn(application);

    Application result = statusTransitionService.changeStatus(1L, ApplicationStatus.WAITING);

    assertEquals(ApplicationStatus.WAITING, result.getStatus());
    verify(applicationRepository).save(application);
    verify(eventPublisher)
        .publishEvent(
            new ApplicationStatusChanged(
                1L, 5L, ApplicationStatus.SUBMITTED, ApplicationStatus.WAITING));
    verify(animalRepository).findByIdForUpdate(5L);
    verify(entityManager).refresh(application);
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
    verifyNoInteractions(eventPublisher);
  }

  @Test
  void missingApplicationShouldThrowException() {
    when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        EntityNotFoundException.class,
        () -> statusTransitionService.changeStatus(99L, ApplicationStatus.WAITING));

    verify(applicationRepository, never()).save(any(Application.class));
    verifyNoInteractions(eventPublisher);
    verifyNoInteractions(animalRepository, entityManager);
  }

  private Application createApplication() {
    return new Application(
        10L, 5L, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-16T10:00:00Z"));
  }
}
