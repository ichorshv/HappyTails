package com.happytails.adoption.enums;

import java.util.Set;

public enum ApplicationStatus {
  SUBMITTED,
  WAITING,
  YOUR_TURN,
  WALK_SCHEDULED,
  APPROVED,
  COMPLETED,
  REJECTED,
  WITHDRAWN,
  EXPIRED;

  /**
   * Statuses of an application that is still in progress. Keep in sync with the partial unique
   * index uq_applications_active_per_animal in the applications migration.
   */
  public static final Set<ApplicationStatus> ACTIVE =
      Set.of(SUBMITTED, WAITING, YOUR_TURN, WALK_SCHEDULED, APPROVED);
}
