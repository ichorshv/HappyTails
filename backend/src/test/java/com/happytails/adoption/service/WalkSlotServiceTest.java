package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.entity.WalkSettings;
import com.happytails.adoption.enums.WalkStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.WalkRepository;
import com.happytails.adoption.repository.WalkSettingsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class WalkSlotServiceTest {

  // Monday 08:00 in Sofia (UTC+2 in November), one hour before the working day starts
  private static final Instant NOW = Instant.parse("2026-11-16T06:00:00Z");

  private final AnimalRepository animalRepository = mock(AnimalRepository.class);
  private final WalkRepository walkRepository = mock(WalkRepository.class);
  private final WalkSettingsRepository settingsRepository = mock(WalkSettingsRepository.class);
  private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

  private final WalkSlotService service =
      new WalkSlotService(
          animalRepository, walkRepository, settingsRepository, new SlotCalculator(clock), clock);

  private void givenSettings() {
    WalkSettings settings = new WalkSettings(1L, LocalTime.of(9, 0), LocalTime.of(11, 0), 60, 2);
    when(settingsRepository.findById(1L)).thenReturn(Optional.of(settings));
  }

  private void givenBookedWalks(List<Walk> walks) {
    when(walkRepository.findByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusOrderByStartsAt(
            any(), any(), eq(WalkStatus.BOOKED)))
        .thenReturn(walks);
  }

  @Test
  @DisplayName("An unknown animal returns 404 with code WALK_ANIMAL_NOT_FOUND")
  void unknownAnimalIsNotFound() {
    when(animalRepository.existsById(99L)).thenReturn(false);

    ApiException error = assertThrows(ApiException.class, () -> service.findFreeSlots(99L));

    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("WALK_ANIMAL_NOT_FOUND", error.getCode());
  }

  @Test
  @DisplayName("Free slots are returned with the animal id and the slot length")
  void freeSlotsAreReturned() {
    when(animalRepository.existsById(1L)).thenReturn(true);
    givenSettings();
    givenBookedWalks(List.of());

    FreeSlotsResponse response = service.findFreeSlots(1L);

    assertEquals(1L, response.animalId());
    assertEquals(60, response.slotMinutes());
    assertEquals(Instant.parse("2026-11-16T07:00:00Z"), response.slots().get(0).startsAt());
  }

  @Test
  @DisplayName("A slot that is already booked for the animal is not returned")
  void bookedSlotIsNotReturned() {
    when(animalRepository.existsById(1L)).thenReturn(true);
    givenSettings();
    Walk booked =
        new Walk(
            5L,
            1L,
            7L,
            Instant.parse("2026-11-16T07:00:00Z"),
            Instant.parse("2026-11-16T08:00:00Z"),
            NOW);
    givenBookedWalks(List.of(booked));

    FreeSlotsResponse response = service.findFreeSlots(1L);

    // the first Monday slot (07:00Z) is taken, so the first free one is 08:00Z
    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), response.slots().get(0).startsAt());
  }

  @Test
  @DisplayName("Missing walk settings is reported as an illegal state")
  void missingSettingsFails() {
    when(animalRepository.existsById(1L)).thenReturn(true);
    when(settingsRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalStateException.class, () -> service.findFreeSlots(1L));
  }
}
