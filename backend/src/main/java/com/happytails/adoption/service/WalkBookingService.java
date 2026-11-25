package com.happytails.adoption.service;

import com.happytails.adoption.dto.BookWalkRequest;
import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.dto.WalkResponse;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.enums.WalkStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.repository.WalkRepository;
import com.happytails.adoption.repository.WalkSettingsRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Books a walk. The database unique indexes are the final guard against double booking. */
@Service
public class WalkBookingService {

  private final ApplicationRepository applicationRepository;
  private final WalkRepository walkRepository;
  private final WalkSettingsRepository walkSettingsRepository;
  private final WalkSlotService walkSlotService;
  private final QueueService queueService;
  private final CurrentUserService currentUserService;
  private final Clock clock;

  public WalkBookingService(
      ApplicationRepository applicationRepository,
      WalkRepository walkRepository,
      WalkSettingsRepository walkSettingsRepository,
      WalkSlotService walkSlotService,
      QueueService queueService,
      CurrentUserService currentUserService,
      Clock clock) {
    this.applicationRepository = applicationRepository;
    this.walkRepository = walkRepository;
    this.walkSettingsRepository = walkSettingsRepository;
    this.walkSlotService = walkSlotService;
    this.queueService = queueService;
    this.currentUserService = currentUserService;
    this.clock = clock;
  }

  @Transactional
  public WalkResponse book(BookWalkRequest request) {
    Application application =
        applicationRepository
            .findById(request.applicationId())
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND,
                        "WALK_APPLICATION_NOT_FOUND",
                        "Заявлението не е намерено."));

    if (!application.getApplicantId().equals(currentUserService.getCurrentUserId())) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "WALK_APPLICATION_FORBIDDEN", "Това заявление не е ваше.");
    }

    if (walkRepository.existsByApplicationIdAndStatus(application.getId(), WalkStatus.BOOKED)) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "WALK_ALREADY_BOOKED",
          "За това заявление вече има запазена разходка.");
    }

    if (!queueService.isFirstInQueue(application.getId())) {
      throw new ApiException(
          HttpStatus.CONFLICT, "WALK_NOT_YOUR_TURN", "Все още не е вашият ред за разходка.");
    }

    // Lock the settings row so concurrent bookings are processed one after another.
    walkSettingsRepository
        .findForUpdate()
        .orElseThrow(() -> new IllegalStateException("Walk settings are missing."));

    Instant startsAt = request.startsAt();
    Long animalId = application.getAnimalId();
    Long applicantId = application.getApplicantId();

    if (walkRepository.existsByAnimalIdAndStartsAtAndStatus(
        animalId, startsAt, WalkStatus.BOOKED)) {
      throw slotTaken();
    }
    if (walkRepository.existsByApplicantIdAndStartsAtAndStatus(
        applicantId, startsAt, WalkStatus.BOOKED)) {
      throw applicantBusy();
    }

    FreeSlotsResponse freeSlots = walkSlotService.findFreeSlots(animalId);
    boolean isFree = freeSlots.slots().stream().anyMatch(s -> s.startsAt().equals(startsAt));
    if (!isFree) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_ENTITY,
          "WALK_SLOT_NOT_AVAILABLE",
          "Избраният час не е наличен.");
    }

    Instant endsAt = startsAt.plus(freeSlots.slotMinutes(), ChronoUnit.MINUTES);
    Walk walk =
        new Walk(application.getId(), animalId, applicantId, startsAt, endsAt, Instant.now(clock));

    try {
      walkRepository.saveAndFlush(walk);
    } catch (DataIntegrityViolationException e) {
      throw translate(e);
    }

    queueService.markWalkScheduled(application.getId());
    return WalkResponse.from(walk);
  }

  private ApiException translate(DataIntegrityViolationException e) {
    String message =
        e.getMostSpecificCause().getMessage() == null ? "" : e.getMostSpecificCause().getMessage();
    if (message.contains("uq_walk_applicant_slot")) {
      return applicantBusy();
    }
    if (message.contains("uq_walk_application")) {
      return new ApiException(
          HttpStatus.CONFLICT,
          "WALK_ALREADY_BOOKED",
          "За това заявление вече има запазена разходка.");
    }
    return slotTaken();
  }

  private ApiException slotTaken() {
    return new ApiException(
        HttpStatus.CONFLICT, "WALK_SLOT_TAKEN", "Този час вече е зает от друг кандидат.");
  }

  private ApiException applicantBusy() {
    return new ApiException(
        HttpStatus.CONFLICT, "WALK_APPLICANT_BUSY", "Вече имате друга разходка по същото време.");
  }
}
