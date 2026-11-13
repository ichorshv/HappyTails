package com.happytails.adoption.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("stub")
public class StubQueueService implements QueueService {

  @Override
  public boolean isFirstInQueue(Long applicationId) {
    return true;
  }

  @Override
  public int getQueuePosition(Long applicationId) {
    return 1;
  }

  @Override
  public void markWalkScheduled(Long applicationId) {
    // TODO: Implement actual queue logic in Sprint 2.
  }
}
