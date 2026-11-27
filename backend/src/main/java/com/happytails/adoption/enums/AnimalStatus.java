package com.happytails.adoption.enums;

public enum AnimalStatus {
  DRAFT,
  AVAILABLE,
  RESERVED,
  ADOPTED;

  /** The only allowed changes: publish, reserve, adoption falls through, hand over. */
  public boolean canChangeTo(AnimalStatus next) {
    return switch (this) {
      case DRAFT -> next == AVAILABLE;
      case AVAILABLE -> next == RESERVED;
      case RESERVED -> next == AVAILABLE || next == ADOPTED;
      case ADOPTED -> false;
    };
  }
}
