package com.happytails.adoption.service;

import com.happytails.adoption.animals.repository.AnimalRepository;
import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.entity.WalkSettings;
import com.happytails.adoption.enums.WalkStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.WalkRepository;
import com.happytails.adoption.repository.WalkSettingsRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalkSlotService {

  private static final long SETTINGS_ID = 1L;

  private final AnimalRepository animalRepository;
  private final WalkRepository walkRepository;
  private final WalkSettingsRepository settingsRepository;
  private final SlotCalculator slotCalculator;
  private final Clock clock;

  public WalkSlotService(
      AnimalRepository animalRepository,
      WalkRepository walkRepository,
      WalkSettingsRepository settingsRepository,
      SlotCalculator slotCalculator,
      Clock clock) {
    this.animalRepository = animalRepository;
    this.walkRepository = walkRepository;
    this.settingsRepository = settingsRepository;
    this.slotCalculator = slotCalculator;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public FreeSlotsResponse findFreeSlots(Long animalId) {
    if (!animalRepository.existsById(animalId)) {
      throw new ApiException(
          HttpStatus.NOT_FOUND, "WALK_ANIMAL_NOT_FOUND", "Животното не е намерено.");
    }
    WalkSettings settings =
        settingsRepository
            .findById(SETTINGS_ID)
            .orElseThrow(() -> new IllegalStateException("Walk settings are missing"));

    Instant from = clock.instant().minus(Duration.ofDays(1));
    Instant to = clock.instant().plus(Duration.ofDays(SlotCalculator.DAYS_AHEAD + 1));
    List<Walk> booked =
        walkRepository.findByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusOrderByStartsAt(
            from, to, WalkStatus.BOOKED);

    return new FreeSlotsResponse(
        animalId,
        settings.getSlotMinutes(),
        slotCalculator.findFreeSlots(settings, animalId, booked));
  }
}
