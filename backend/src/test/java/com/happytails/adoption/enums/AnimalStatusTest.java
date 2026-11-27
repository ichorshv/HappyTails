package com.happytails.adoption.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AnimalStatusTest {

  @Test
  void allowsOnlyTheFourTransitions() {
    assertTrue(AnimalStatus.DRAFT.canChangeTo(AnimalStatus.AVAILABLE));
    assertTrue(AnimalStatus.AVAILABLE.canChangeTo(AnimalStatus.RESERVED));
    assertTrue(AnimalStatus.RESERVED.canChangeTo(AnimalStatus.AVAILABLE));
    assertTrue(AnimalStatus.RESERVED.canChangeTo(AnimalStatus.ADOPTED));
  }

  @Test
  void rejectsSkippingSteps() {
    assertFalse(AnimalStatus.DRAFT.canChangeTo(AnimalStatus.RESERVED));
    assertFalse(AnimalStatus.DRAFT.canChangeTo(AnimalStatus.ADOPTED));
    assertFalse(AnimalStatus.AVAILABLE.canChangeTo(AnimalStatus.ADOPTED));
  }

  @Test
  void rejectsGoingBack() {
    assertFalse(AnimalStatus.AVAILABLE.canChangeTo(AnimalStatus.DRAFT));
    assertFalse(AnimalStatus.RESERVED.canChangeTo(AnimalStatus.DRAFT));
  }

  @Test
  void adoptedIsFinal() {
    for (AnimalStatus next : AnimalStatus.values()) {
      assertFalse(AnimalStatus.ADOPTED.canChangeTo(next));
    }
  }

  @Test
  void rejectsStayingInTheSameStatus() {
    for (AnimalStatus status : AnimalStatus.values()) {
      assertFalse(status.canChangeTo(status));
    }
  }
}
