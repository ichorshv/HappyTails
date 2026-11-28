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

class QueueAdvancementServiceTest {

  private ApplicationRepository applicationRepository;
  private AnimalRepository animalRepository;
  private StatusTransitionService statusTransitionService;
  private EntityManager entityManager;
  private QueueAdvancementService service;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    animalRepository = mock(AnimalRepository.class);
    statusTransitionService = mock(StatusTransitionService.class);
    entityManager = mock(EntityManager.class);

    service =
        new QueueAdvancementService(
            applicationRepository, animalRepository, entityManager, statusTransitionService);

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

  private Application createApplication(Long id, ApplicationStatus status, Instant deadline) {

    Application app =
        new Application(
            id + 100L,
            100L,
            "Test Street",
            "Sofia",
            "Sofia",
            Instant.parse("2026-11-01T10:00:00Z").plusSeconds(id * 60));

    app.setStatus(status);
    app.setTurnDeadline(deadline);

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
  void expiredCandidateAdvancesNextWaitingCandidate() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.now().minusSeconds(60));

    Application second = createApplication(2L, ApplicationStatus.WAITING, null);

    when(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(100L))
        .thenReturn(List.of(first, second));

    Instant before = Instant.now();

    boolean result = service.expireAndAdvance(1L);

    Instant after = Instant.now();

    assertTrue(result);
    assertEquals(ApplicationStatus.EXPIRED, first.getStatus());
    assertNull(first.getTurnDeadline());

    assertEquals(ApplicationStatus.YOUR_TURN, second.getStatus());
    assertNotNull(second.getTurnDeadline());

    assertFalse(second.getTurnDeadline().isBefore(before.plusSeconds(48 * 3600)));
    assertFalse(second.getTurnDeadline().isAfter(after.plusSeconds(48 * 3600)));

    verify(animalRepository).findByIdForUpdate(100L);
    verify(entityManager).refresh(first);

    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.EXPIRED);

    verify(statusTransitionService).changeStatus(2L, ApplicationStatus.YOUR_TURN);
  }

  @Test
  void doesNotExpireCandidateBeforeDeadline() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.now().plusSeconds(3600));

    boolean result = service.expireAndAdvance(1L);

    assertFalse(result);
    assertEquals(ApplicationStatus.YOUR_TURN, first.getStatus());

    verifyNoInteractions(statusTransitionService);

    verify(applicationRepository, never()).findByAnimalIdOrderBySubmittedAtAscIdAsc(anyLong());
  }

  @Test
  void doesNotProcessAlreadyExpiredCandidate() {
    Application first =
        createApplication(1L, ApplicationStatus.EXPIRED, Instant.now().minusSeconds(3600));

    boolean result = service.expireAndAdvance(1L);

    assertFalse(result);
    assertEquals(ApplicationStatus.EXPIRED, first.getStatus());

    verifyNoInteractions(statusTransitionService);
  }

  @Test
  void expiresCandidateWithoutWaitingApplications() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.now().minusSeconds(60));

    when(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(100L))
        .thenReturn(List.of(first));

    boolean result = service.expireAndAdvance(1L);

    assertTrue(result);
    assertEquals(ApplicationStatus.EXPIRED, first.getStatus());
    assertNull(first.getTurnDeadline());

    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.EXPIRED);

    verify(statusTransitionService, never())
        .changeStatus(anyLong(), eq(ApplicationStatus.YOUR_TURN));
  }
}
