package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QueueServiceImplTest {

  @Mock private ApplicationRepository applicationRepository;

  @Mock private StatusTransitionService statusTransitionService;

  private QueueServiceImpl queueService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    queueService = new QueueServiceImpl(applicationRepository, statusTransitionService);
  }

  private Application createApplication(Long id, ApplicationStatus status, Instant submittedAt) {

    Application app = new Application(10L, 100L, "Street", "Sofia", "Sofia", submittedAt);

    app.setStatus(status);

    try {
      var field = Application.class.getDeclaredField("id");
      field.setAccessible(true);
      field.set(app, id);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }

    return app;
  }

  @Test
  void returnsCorrectQueuePosition() {
    Instant time = Instant.parse("2026-11-01T10:00:00Z");

    Application first = createApplication(1L, ApplicationStatus.YOUR_TURN, time);

    Application second = createApplication(2L, ApplicationStatus.WAITING, time.plusSeconds(60));

    when(applicationRepository.findById(2L)).thenReturn(Optional.of(second));

    when(applicationRepository.findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
            eq(100L), anyCollection()))
        .thenReturn(List.of(first, second));

    assertEquals(2, queueService.getQueuePosition(2L));
  }

  @Test
  void returnsZeroForWithdrawnApplication() {
    Application withdrawn =
        createApplication(3L, ApplicationStatus.WITHDRAWN, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findById(3L)).thenReturn(Optional.of(withdrawn));

    assertEquals(0, queueService.getQueuePosition(3L));

    verify(applicationRepository, never())
        .findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(anyLong(), anyCollection());
  }

  @Test
  void checksFirstInQueue() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(first));

    when(applicationRepository.findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
            eq(100L), anyCollection()))
        .thenReturn(List.of(first));

    assertTrue(queueService.isFirstInQueue(1L));
  }

  @Test
  void schedulesWalkOnlyForFirstCandidate() {
    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findById(1L)).thenReturn(Optional.of(first));

    when(applicationRepository.findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
            eq(100L), anyCollection()))
        .thenReturn(List.of(first));

    queueService.markWalkScheduled(1L);

    verify(statusTransitionService).changeStatus(1L, ApplicationStatus.WALK_SCHEDULED);
  }

  @Test
  void rejectsWalkWhenNotFirst() {
    Application second =
        createApplication(2L, ApplicationStatus.WAITING, Instant.parse("2026-11-01T10:01:00Z"));

    Application first =
        createApplication(1L, ApplicationStatus.YOUR_TURN, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findById(2L)).thenReturn(Optional.of(second));

    when(applicationRepository.findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
            eq(100L), anyCollection()))
        .thenReturn(List.of(first, second));

    assertThrows(IllegalStateException.class, () -> queueService.markWalkScheduled(2L));

    verifyNoInteractions(statusTransitionService);
  }

  @Test
  void rejectsWalkWhenStatusIsWaitingEvenIfFirst() {
    Application waiting =
        createApplication(5L, ApplicationStatus.WAITING, Instant.parse("2026-11-01T10:00:00Z"));

    when(applicationRepository.findById(5L)).thenReturn(Optional.of(waiting));

    assertThrows(IllegalStateException.class, () -> queueService.markWalkScheduled(5L));

    verifyNoInteractions(statusTransitionService);
  }
}
