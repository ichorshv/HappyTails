package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueueExpirationSchedulerTest {

  private ApplicationRepository applicationRepository;
  private QueueAdvancementService queueAdvancementService;
  private QueueExpirationScheduler scheduler;

  @BeforeEach
  void setUp() {
    applicationRepository = mock(ApplicationRepository.class);
    queueAdvancementService = mock(QueueAdvancementService.class);

    scheduler = new QueueExpirationScheduler(applicationRepository, queueAdvancementService);
  }

  @Test
  void processesAllExpiredApplications() {
    Application first = mock(Application.class);
    Application second = mock(Application.class);

    when(first.getId()).thenReturn(1L);
    when(second.getId()).thenReturn(2L);

    when(applicationRepository.findByStatusAndTurnDeadlineLessThanEqual(
            eq(ApplicationStatus.YOUR_TURN), any(Instant.class)))
        .thenReturn(List.of(first, second));

    when(queueAdvancementService.expireAndAdvance(1L)).thenReturn(true);
    when(queueAdvancementService.expireAndAdvance(2L)).thenReturn(true);

    scheduler.checkExpiredApplications();

    verify(queueAdvancementService).expireAndAdvance(1L);
    verify(queueAdvancementService).expireAndAdvance(2L);
  }

  @Test
  void continuesProcessingAfterOneApplicationFails() {
    Application first = mock(Application.class);
    Application second = mock(Application.class);

    when(first.getId()).thenReturn(1L);
    when(second.getId()).thenReturn(2L);

    when(applicationRepository.findByStatusAndTurnDeadlineLessThanEqual(
            eq(ApplicationStatus.YOUR_TURN), any(Instant.class)))
        .thenReturn(List.of(first, second));

    when(queueAdvancementService.expireAndAdvance(1L))
        .thenThrow(new RuntimeException("Simulated processing failure"));

    when(queueAdvancementService.expireAndAdvance(2L)).thenReturn(true);

    assertDoesNotThrow(() -> scheduler.checkExpiredApplications());

    verify(queueAdvancementService).expireAndAdvance(1L);
    verify(queueAdvancementService).expireAndAdvance(2L);
  }

  @Test
  void doesNothingWhenNoApplicationsAreExpired() {
    when(applicationRepository.findByStatusAndTurnDeadlineLessThanEqual(
            eq(ApplicationStatus.YOUR_TURN), any(Instant.class)))
        .thenReturn(List.of());

    scheduler.checkExpiredApplications();

    verifyNoInteractions(queueAdvancementService);
  }
}
