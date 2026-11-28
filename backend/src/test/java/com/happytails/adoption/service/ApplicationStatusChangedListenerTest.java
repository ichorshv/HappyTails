package com.happytails.adoption.service;

import static org.mockito.Mockito.*;

import com.happytails.adoption.enums.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationStatusChangedListenerTest {

  private QueueAdvancementService queueAdvancementService;
  private ApplicationStatusChangedListener listener;

  @BeforeEach
  void setUp() {
    queueAdvancementService = mock(QueueAdvancementService.class);

    listener = new ApplicationStatusChangedListener(queueAdvancementService);
  }

  @Test
  void advancesQueueWhenApplicationIsWithdrawn() {
    ApplicationStatusChanged event =
        new ApplicationStatusChanged(
            1L, 100L, ApplicationStatus.YOUR_TURN, ApplicationStatus.WITHDRAWN);

    listener.onApplicationStatusChanged(event);

    verify(queueAdvancementService).advanceQueue(100L);
  }

  @Test
  void advancesQueueWhenApplicationIsRejected() {
    ApplicationStatusChanged event =
        new ApplicationStatusChanged(
            1L, 100L, ApplicationStatus.YOUR_TURN, ApplicationStatus.REJECTED);

    listener.onApplicationStatusChanged(event);

    verify(queueAdvancementService).advanceQueue(100L);
  }

  @Test
  void ignoresOtherStatusChanges() {
    ApplicationStatusChanged event =
        new ApplicationStatusChanged(
            1L, 100L, ApplicationStatus.WAITING, ApplicationStatus.YOUR_TURN);

    listener.onApplicationStatusChanged(event);

    verifyNoInteractions(queueAdvancementService);
  }
}
