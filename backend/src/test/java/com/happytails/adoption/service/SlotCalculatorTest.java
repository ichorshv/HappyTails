package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.happytails.adoption.dto.SlotDto;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.entity.WalkSettings;
import com.happytails.adoption.enums.WalkStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotCalculatorTest {

  // Sofia is UTC+2 in November, so 09:00 local time is 07:00Z.
  private static final Instant MONDAY_0800 = Instant.parse("2026-11-16T06:00:00Z");

  private static SlotCalculator calculatorAt(Instant now) {
    return new SlotCalculator(Clock.fixed(now, ZoneOffset.UTC));
  }

  private static WalkSettings settings(String start, String end, int minutes, int staff) {
    return new WalkSettings(1L, LocalTime.parse(start), LocalTime.parse(end), minutes, staff);
  }

  private static WalkSettings defaultSettings() {
    return settings("09:00", "11:00", 60, 2);
  }

  private static Walk walk(long animalId, String startsAt, String endsAt) {
    return new Walk(1L, animalId, 1L, Instant.parse(startsAt), Instant.parse(endsAt), MONDAY_0800);
  }

  @Test
  @DisplayName("Slots are built from the working hours and the slot length")
  void slotsAreBuiltFromWorkingHours() {
    List<SlotDto> slots = calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, List.of());

    assertEquals(Instant.parse("2026-11-16T07:00:00Z"), slots.get(0).startsAt());
    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).endsAt());
    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(1).startsAt());
    assertEquals(Instant.parse("2026-11-16T09:00:00Z"), slots.get(1).endsAt());
  }

  @Test
  @DisplayName("The last slot of the day must fit before the end of work")
  void lastSlotMustFitBeforeWorkEnd() {
    List<SlotDto> slots =
        calculatorAt(MONDAY_0800).findFreeSlots(settings("09:00", "11:30", 60, 2), 1L, List.of());

    // 10 working days x 2 slots (09-10 and 10-11), the 11:00-12:00 slot does not fit
    assertEquals(20, slots.size());
    assertEquals(Instant.parse("2026-11-27T09:00:00Z"), slots.get(19).endsAt());
  }

  @Test
  @DisplayName("Saturdays and Sundays are skipped")
  void weekendsAreSkipped() {
    Instant fridayMorning = Instant.parse("2026-11-20T06:00:00Z");

    List<SlotDto> slots =
        calculatorAt(fridayMorning).findFreeSlots(defaultSettings(), 1L, List.of());

    // two slots on Friday, then the next one is on Monday
    assertEquals(Instant.parse("2026-11-20T08:00:00Z"), slots.get(1).startsAt());
    assertEquals(Instant.parse("2026-11-23T07:00:00Z"), slots.get(2).startsAt());
  }

  @Test
  @DisplayName("A slot that already started is not shown")
  void pastSlotsAreNotShown() {
    Instant mondayHalfPastNine = Instant.parse("2026-11-16T07:30:00Z");

    List<SlotDto> slots =
        calculatorAt(mondayHalfPastNine).findFreeSlots(defaultSettings(), 1L, List.of());

    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A slot that starts exactly now is not shown")
  void slotStartingNowIsNotShown() {
    Instant mondayNine = Instant.parse("2026-11-16T07:00:00Z");

    List<SlotDto> slots = calculatorAt(mondayNine).findFreeSlots(defaultSettings(), 1L, List.of());

    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("After the end of the working day the next slot is on the next day")
  void afterWorkEndNextSlotIsTomorrow() {
    Instant mondayNoon = Instant.parse("2026-11-16T10:00:00Z");

    List<SlotDto> slots = calculatorAt(mondayNoon).findFreeSlots(defaultSettings(), 1L, List.of());

    assertEquals(Instant.parse("2026-11-17T07:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A booked slot of the same animal is not shown")
  void bookedSlotOfSameAnimalIsNotShown() {
    List<Walk> walks = List.of(walk(1L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z"));

    List<SlotDto> slots = calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, walks);

    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A walk that overlaps only part of a slot still blocks it")
  void partiallyOverlappingWalkBlocksSlot() {
    List<Walk> walks = List.of(walk(1L, "2026-11-16T07:30:00Z", "2026-11-16T08:30:00Z"));

    List<SlotDto> slots = calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, walks);

    // both slots on Monday are blocked, the first free one is on Tuesday
    assertEquals(Instant.parse("2026-11-17T07:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A walk of another animal does not block the slot while staff is available")
  void otherAnimalDoesNotBlockWhileStaffIsAvailable() {
    List<Walk> walks = List.of(walk(2L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z"));

    List<SlotDto> slots = calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, walks);

    assertEquals(Instant.parse("2026-11-16T07:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("When all staff members are busy the slot is not shown")
  void fullCapacityBlocksSlot() {
    List<Walk> walks =
        List.of(
            walk(2L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z"),
            walk(3L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z"));

    List<SlotDto> slots = calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, walks);

    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("With a single staff member one walk of another animal is enough to block")
  void singleStaffMemberBlocksAfterOneWalk() {
    List<Walk> walks = List.of(walk(2L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z"));

    List<SlotDto> slots =
        calculatorAt(MONDAY_0800).findFreeSlots(settings("09:00", "11:00", 60, 1), 1L, walks);

    assertEquals(Instant.parse("2026-11-16T08:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A cancelled walk does not block the slot")
  void cancelledWalkDoesNotBlock() {
    Walk cancelled = walk(1L, "2026-11-16T07:00:00Z", "2026-11-16T08:00:00Z");
    cancelled.setStatus(WalkStatus.CANCELLED);

    List<SlotDto> slots =
        calculatorAt(MONDAY_0800).findFreeSlots(defaultSettings(), 1L, List.of(cancelled));

    assertEquals(Instant.parse("2026-11-16T07:00:00Z"), slots.get(0).startsAt());
  }

  @Test
  @DisplayName("A slot length that is not positive is rejected")
  void invalidSlotLengthIsRejected() {
    SlotCalculator calculator = calculatorAt(MONDAY_0800);
    WalkSettings invalid = settings("09:00", "11:00", 0, 2);

    assertThrows(
        IllegalArgumentException.class, () -> calculator.findFreeSlots(invalid, 1L, List.of()));
  }
}
