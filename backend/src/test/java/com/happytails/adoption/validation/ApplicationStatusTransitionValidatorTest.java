package com.happytails.adoption.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.happytails.adoption.enums.ApplicationStatus;
import org.junit.jupiter.api.Test;

class ApplicationStatusTransitionValidatorTest {

  private final ApplicationStatusTransitionValidator validator =
      new ApplicationStatusTransitionValidator();

  @Test
  void allAllowedTransitionsShouldPass() {
    ApplicationStatus[][] allowed = {
      {ApplicationStatus.SUBMITTED, ApplicationStatus.WAITING},
      {ApplicationStatus.SUBMITTED, ApplicationStatus.YOUR_TURN},
      {ApplicationStatus.SUBMITTED, ApplicationStatus.REJECTED},
      {ApplicationStatus.WAITING, ApplicationStatus.YOUR_TURN},
      {ApplicationStatus.WAITING, ApplicationStatus.WITHDRAWN},
      {ApplicationStatus.WAITING, ApplicationStatus.REJECTED},
      {ApplicationStatus.YOUR_TURN, ApplicationStatus.WALK_SCHEDULED},
      {ApplicationStatus.YOUR_TURN, ApplicationStatus.EXPIRED},
      {ApplicationStatus.YOUR_TURN, ApplicationStatus.WITHDRAWN},
      {ApplicationStatus.YOUR_TURN, ApplicationStatus.REJECTED},
      {ApplicationStatus.WALK_SCHEDULED, ApplicationStatus.APPROVED},
      {ApplicationStatus.WALK_SCHEDULED, ApplicationStatus.REJECTED},
      {ApplicationStatus.WALK_SCHEDULED, ApplicationStatus.WITHDRAWN},
      {ApplicationStatus.WALK_SCHEDULED, ApplicationStatus.YOUR_TURN},
      {ApplicationStatus.APPROVED, ApplicationStatus.COMPLETED},
      {ApplicationStatus.APPROVED, ApplicationStatus.WITHDRAWN}
    };

    assertEquals(16, allowed.length);

    for (ApplicationStatus[] transition : allowed) {
      assertTrue(
          validator.isAllowed(transition[0], transition[1]),
          () -> "Transition should be allowed: " + transition[0] + " -> " + transition[1]);

      assertDoesNotThrow(() -> validator.validate(transition[0], transition[1]));
    }
  }

  @Test
  void forbiddenTransitionsShouldBeRejected() {
    ApplicationStatus[][] forbidden = {
      {ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED},
      {ApplicationStatus.WAITING, ApplicationStatus.COMPLETED},
      {ApplicationStatus.COMPLETED, ApplicationStatus.WAITING},
      {ApplicationStatus.REJECTED, ApplicationStatus.APPROVED},
      {ApplicationStatus.EXPIRED, ApplicationStatus.YOUR_TURN},
      {ApplicationStatus.WITHDRAWN, ApplicationStatus.SUBMITTED}
    };

    for (ApplicationStatus[] transition : forbidden) {
      assertFalse(validator.isAllowed(transition[0], transition[1]));

      assertThrows(
          IllegalArgumentException.class, () -> validator.validate(transition[0], transition[1]));
    }
  }

  @Test
  void terminalStatusesShouldHaveNoOutgoingTransitions() {
    ApplicationStatus[] terminal = {
      ApplicationStatus.COMPLETED,
      ApplicationStatus.REJECTED,
      ApplicationStatus.WITHDRAWN,
      ApplicationStatus.EXPIRED
    };

    for (ApplicationStatus from : terminal) {
      for (ApplicationStatus to : ApplicationStatus.values()) {
        assertFalse(validator.isAllowed(from, to), () -> from + " must be terminal");
      }
    }
  }

  @Test
  void nullStatusesShouldBeRejected() {
    assertFalse(validator.isAllowed(null, ApplicationStatus.WAITING));
    assertFalse(validator.isAllowed(ApplicationStatus.WAITING, null));
    assertFalse(validator.isAllowed(null, null));

    assertThrows(
        IllegalArgumentException.class, () -> validator.validate(null, ApplicationStatus.WAITING));
  }
}
