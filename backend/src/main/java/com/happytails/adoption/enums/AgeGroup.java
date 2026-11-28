package com.happytails.adoption.enums;

public enum AgeGroup {
  PUPPY(0, 11),
  YOUNG(12, 35),
  ADULT(36, Integer.MAX_VALUE);

  private final int minMonths;
  private final int maxMonths;

  AgeGroup(int minMonths, int maxMonths) {
    this.minMonths = minMonths;
    this.maxMonths = maxMonths;
  }

  public int getMinMonths() {
    return minMonths;
  }

  public int getMaxMonths() {
    return maxMonths;
  }
}
