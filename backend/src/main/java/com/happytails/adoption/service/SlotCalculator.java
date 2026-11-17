package com.happytails.adoption.service;

import com.happytails.adoption.dto.SlotDto;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.entity.WalkSettings;
import com.happytails.adoption.enums.WalkStatus;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SlotCalculator {

  public static final int DAYS_AHEAD = 14;
  private static final ZoneId SHELTER_ZONE = ZoneId.of("Europe/Sofia");

  private final Clock clock;

  public SlotCalculator(Clock clock) {
    this.clock = clock;
  }

  public List<SlotDto> findFreeSlots(WalkSettings settings, Long animalId, List<Walk> walks) {
    Instant now = clock.instant();
    Instant windowEnd = now.plus(Duration.ofDays(DAYS_AHEAD));
    LocalDate today = LocalDate.ofInstant(now, SHELTER_ZONE);

    List<SlotDto> result = new ArrayList<>();
    for (int day = 0; day <= DAYS_AHEAD; day++) {
      LocalDate date = today.plusDays(day);
      if (isWeekend(date)) {
        continue;
      }
      for (SlotDto slot : slotsOfDay(settings, date)) {
        boolean inWindow = slot.startsAt().isAfter(now) && slot.startsAt().isBefore(windowEnd);
        if (inWindow && isFree(slot, settings, animalId, walks)) {
          result.add(slot);
        }
      }
    }
    return result;
  }

  private boolean isWeekend(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  private List<SlotDto> slotsOfDay(WalkSettings settings, LocalDate date) {
    int length = settings.getSlotMinutes();
    if (length <= 0) {
      throw new IllegalArgumentException("slotMinutes must be positive");
    }
    int startMinute = settings.getWorkStart().toSecondOfDay() / 60;
    int endMinute = settings.getWorkEnd().toSecondOfDay() / 60;

    List<SlotDto> slots = new ArrayList<>();
    for (int minute = startMinute; minute + length <= endMinute; minute += length) {
      slots.add(new SlotDto(toInstant(date, minute), toInstant(date, minute + length)));
    }
    return slots;
  }

  private Instant toInstant(LocalDate date, int minuteOfDay) {
    return date.atStartOfDay().plusMinutes(minuteOfDay).atZone(SHELTER_ZONE).toInstant();
  }

  private boolean isFree(SlotDto slot, WalkSettings settings, Long animalId, List<Walk> walks) {
    int overlapping = 0;
    for (Walk walk : walks) {
      if (walk.getStatus() != WalkStatus.BOOKED || !overlaps(walk, slot)) {
        continue;
      }
      if (walk.getAnimalId().equals(animalId)) {
        return false;
      }
      overlapping++;
    }
    return overlapping < settings.getStaffCount();
  }

  private boolean overlaps(Walk walk, SlotDto slot) {
    return walk.getStartsAt().isBefore(slot.endsAt()) && walk.getEndsAt().isAfter(slot.startsAt());
  }
}
