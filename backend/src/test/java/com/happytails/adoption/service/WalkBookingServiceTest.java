package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.BookWalkRequest;
import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.dto.SlotDto;
import com.happytails.adoption.dto.WalkResponse;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.entity.WalkSettings;
import com.happytails.adoption.enums.WalkStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.repository.WalkRepository;
import com.happytails.adoption.repository.WalkSettingsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

class WalkBookingServiceTest {

  private static final Instant NOW = Instant.parse("2026-11-16T06:00:00Z");
  private static final Instant START = Instant.parse("2026-11-17T07:00:00Z");
  private static final Instant END = Instant.parse("2026-11-17T08:00:00Z");
  private static final long APPLICATION_ID = 15L;
  private static final long ANIMAL_ID = 7L;
  private static final long USER_ID = 3L;

  private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
  private final WalkRepository walkRepository = mock(WalkRepository.class);
  private final WalkSettingsRepository settingsRepository = mock(WalkSettingsRepository.class);
  private final WalkSlotService slotService = mock(WalkSlotService.class);
  private final QueueService queueService = mock(QueueService.class);
  private final CurrentUserService currentUserService = mock(CurrentUserService.class);

  private WalkBookingService service;
  private Application application;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    service =
        new WalkBookingService(
            applicationRepository,
            walkRepository,
            settingsRepository,
            slotService,
            queueService,
            currentUserService,
            clock);

    application = new Application(USER_ID, ANIMAL_ID, "ул. Тест 1", "София", "София", NOW);
    ReflectionTestUtils.setField(application, "id", APPLICATION_ID);

    when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
    when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
    when(queueService.isFirstInQueue(APPLICATION_ID)).thenReturn(true);
    when(settingsRepository.findForUpdate()).thenReturn(Optional.of(mock(WalkSettings.class)));
    when(slotService.findFreeSlots(ANIMAL_ID))
        .thenReturn(new FreeSlotsResponse(ANIMAL_ID, 60, List.of(new SlotDto(START, END))));
    when(walkRepository.saveAndFlush(any(Walk.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  private ApiException bookAndExpectError() {
    return assertThrows(
        ApiException.class, () -> service.book(new BookWalkRequest(APPLICATION_ID, START)));
  }

  @Test
  @DisplayName("The first applicant in the queue books a free slot")
  void booksFreeSlot() {
    WalkResponse response = service.book(new BookWalkRequest(APPLICATION_ID, START));

    assertEquals(START, response.startsAt());
    assertEquals(END, response.endsAt());
    assertEquals(ANIMAL_ID, response.animalId());
    assertEquals(WalkStatus.BOOKED, response.status());
    verify(queueService).markWalkScheduled(APPLICATION_ID);
  }

  @Test
  @DisplayName("An unknown application returns 404")
  void unknownApplication() {
    when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("WALK_APPLICATION_NOT_FOUND", error.getCode());
  }

  @Test
  @DisplayName("Someone else's application returns 403")
  void foreignApplication() {
    when(currentUserService.getCurrentUserId()).thenReturn(99L);

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    assertEquals("WALK_APPLICATION_FORBIDDEN", error.getCode());
  }

  @Test
  @DisplayName("An applicant who is not first in the queue cannot book")
  void notYourTurn() {
    when(queueService.isFirstInQueue(APPLICATION_ID)).thenReturn(false);

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("WALK_NOT_YOUR_TURN", error.getCode());
    verify(walkRepository, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("An application with a booked walk cannot book again")
  void alreadyBooked() {
    when(walkRepository.existsByApplicationIdAndStatus(APPLICATION_ID, WalkStatus.BOOKED))
        .thenReturn(true);

    ApiException error = bookAndExpectError();

    assertEquals("WALK_ALREADY_BOOKED", error.getCode());
    verify(walkRepository, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("A slot already taken for the animal is rejected")
  void slotTakenForAnimal() {
    when(walkRepository.existsByAnimalIdAndStartsAtAndStatus(ANIMAL_ID, START, WalkStatus.BOOKED))
        .thenReturn(true);

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("WALK_SLOT_TAKEN", error.getCode());
    verify(walkRepository, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("An applicant cannot have two walks at the same time")
  void applicantBusy() {
    when(walkRepository.existsByApplicantIdAndStartsAtAndStatus(USER_ID, START, WalkStatus.BOOKED))
        .thenReturn(true);

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("WALK_APPLICANT_BUSY", error.getCode());
    verify(walkRepository, never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("A time that is not a free slot returns 422")
  void slotNotAvailable() {
    when(slotService.findFreeSlots(ANIMAL_ID))
        .thenReturn(new FreeSlotsResponse(ANIMAL_ID, 60, List.of()));

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getStatus());
    assertEquals("WALK_SLOT_NOT_AVAILABLE", error.getCode());
  }

  @Test
  @DisplayName(
      "When the database rejects a duplicate slot, the answer is 409 and the queue is untouched")
  void databaseUniqueViolation() {
    when(walkRepository.saveAndFlush(any(Walk.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate key uq_walk_animal_slot"));

    ApiException error = bookAndExpectError();

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("WALK_SLOT_TAKEN", error.getCode());
    verify(queueService, never()).markWalkScheduled(any());
  }
}
