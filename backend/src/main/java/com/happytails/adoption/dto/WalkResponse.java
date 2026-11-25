package com.happytails.adoption.dto;

import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.enums.WalkStatus;
import java.time.Instant;

public record WalkResponse(
    Long id,
    Long applicationId,
    Long animalId,
    Instant startsAt,
    Instant endsAt,
    WalkStatus status) {

  public static WalkResponse from(Walk walk) {
    return new WalkResponse(
        walk.getId(),
        walk.getApplicationId(),
        walk.getAnimalId(),
        walk.getStartsAt(),
        walk.getEndsAt(),
        walk.getStatus());
  }
}
