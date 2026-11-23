package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationQueueEntryServiceTest {

  private ApplicationRepository applicationRepository;
  private AnimalRepository animalRepository;
  private StatusTransitionService statusTransitionService;
  private EntityManager entityManager;
  private ApplicationQueueEntryService service;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    animalRepository = mock(AnimalRepository.class);
    statusTransitionService = mock(StatusTransitionService.class);
    entityManager = mock(EntityManager.class);

    service =
        new ApplicationQueueEntryService(
            applicationRepository, animalRepository, statusTransitionService, entityManager);

    when(animalRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(mock(Animal.class)));

    when(applicationRepository.save(any(Application.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    when(statusTransitionService.changeStatus(anyLong(), any(ApplicationStatus.class)))
        .thenAnswer(
            invocation -> {
              Long id = invocation.getArgument(0);
              ApplicationStatus newStatus = invocation.getArgument(1);

              Application app = applicationRepository.findById(id).orElseThrow();

              app.setStatus(newStatus);
              return app;
            });
  }

  private Application createApplication(Long id, ApplicationStatus status, Instant submittedAt) {

    Application app =
        new Application(id + 100L, 100L, "Test Street", "Sofia", "Sofia", submittedAt);

    app.setStatus(status);

    try {
      var field = Application.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(app, id);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }

    when(applicationRepository.findById(id)).thenReturn(Optional.of(app));

    return app;
  }

  @Test
  void firstCandidateGetsYourTurnAnd48HourDeadline() {
    Application first =
        createApplication(1L, ApplicationStatus.SUBMITTED, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(100L))
        .thenReturn(List.of(first));

    Instant before = Instant.now();

    Application result = service.onApplicationValidated(1L);

    Instant after = Instant.now();

    assertEquals(ApplicationStatus.YOUR_TURN, result.getStatus());
    assertNotNull(result.getTurnDeadline());

    assertFalse(result.getTurnDeadline().isBefore(before.plusSeconds(48 * 3600)));

    assertFalse(result.getTurnDeadline().isAfter(after.plusSeconds(48 * 3600)));

    verify(animalRepository).findByIdForUpdate(100L);
    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.YOUR_TURN);
  }

  @Test
  void secondCandidateGetsWaiting() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.parse("2026-11-01T10:00:00Z"));

    Application second =
        createApplication(2L, ApplicationStatus.SUBMITTED, Instant.parse("2026-11-01T11:00:00Z"));

    when(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(100L))
        .thenReturn(List.of(first, second));

    Application result = service.onApplicationValidated(2L);

    assertEquals(ApplicationStatus.WAITING, result.getStatus());
    assertNull(result.getTurnDeadline());

    verify(statusTransitionService).changeStatus(2L, ApplicationStatus.WAITING);
  }

  @Test
  void earlierUnvalidatedCandidateKeepsLaterCandidateWaiting() {
    Application first =
        createApplication(1L, ApplicationStatus.SUBMITTED, Instant.parse("2026-11-01T10:00:00Z"));

    Application second =
        createApplication(2L, ApplicationStatus.SUBMITTED, Instant.parse("2026-11-01T11:00:00Z"));

    when(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(100L))
        .thenReturn(List.of(first, second));

    Application result = service.onApplicationValidated(2L);

    assertEquals(ApplicationStatus.WAITING, result.getStatus());
    assertNull(result.getTurnDeadline());
  }

  @Test
  void rejectsApplicationThatIsNotSubmitted() {
    Application application =
        createApplication(1L, ApplicationStatus.WAITING, Instant.parse("2026-11-01T10:00:00Z"));

    assertThrows(
        IllegalStateException.class, () -> service.onApplicationValidated(application.getId()));

    verifyNoInteractions(statusTransitionService);
  }
}
