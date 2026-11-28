package com.happytails.adoption.service;

import com.happytails.adoption.enums.ApplicationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ApplicationStatusChangedListener {

  private final QueueAdvancementService queueAdvancementService;

  public ApplicationStatusChangedListener(QueueAdvancementService queueAdvancementService) {
    this.queueAdvancementService = queueAdvancementService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onApplicationStatusChanged(ApplicationStatusChanged event) {

    ApplicationStatus newStatus = event.newStatus();

    if (newStatus != ApplicationStatus.WITHDRAWN && newStatus != ApplicationStatus.REJECTED) {
      return;
    }

    queueAdvancementService.advanceQueue(event.animalId());
  }
}
