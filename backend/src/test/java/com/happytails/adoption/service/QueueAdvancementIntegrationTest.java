package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.Species;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QueueAdvancementIntegrationTest {

  @Autowired private AnimalRepository animalRepository;
  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private StatusTransitionService statusTransitionService;

  @ParameterizedTest
  @EnumSource(
      value = ApplicationStatus.class,
      names = {"REJECTED", "WITHDRAWN"})
  void removingFirstCandidateAdvancesNextAfterCommit(ApplicationStatus exitStatus) {

    Long animalId = null;
    Long firstId = null;
    Long secondId = null;

    try {
      Animal animal =
          new Animal(
              "Advancement Test Dog",
              Species.DOG,
              24,
              AnimalSize.MEDIUM,
              "Animal for queue advancement testing",
              true,
              true,
              null,
              Set.of(),
              Instant.now());

      animalId = animalRepository.saveAndFlush(animal).getId();

      Instant submittedAt = Instant.parse("2026-11-01T10:00:00Z");

      Application first =
          new Application(901L, animalId, "Street 1", "Sofia", "Sofia", submittedAt);

      first.setStatus(ApplicationStatus.YOUR_TURN);
      first.setTurnDeadline(Instant.now().plusSeconds(3600));

      firstId = applicationRepository.saveAndFlush(first).getId();

      Application second =
          new Application(
              902L, animalId, "Street 2", "Sofia", "Sofia", submittedAt.plusSeconds(60));

      second.setStatus(ApplicationStatus.WAITING);

      secondId = applicationRepository.saveAndFlush(second).getId();

      Instant before = Instant.now();

      // This call commits its transaction and publishes
      // ApplicationStatusChanged.
      statusTransitionService.changeStatus(firstId, exitStatus);

      Instant after = Instant.now();

      Application updatedFirst = applicationRepository.findById(firstId).orElseThrow();

      Application updatedSecond = applicationRepository.findById(secondId).orElseThrow();

      assertEquals(exitStatus, updatedFirst.getStatus());

      assertEquals(ApplicationStatus.YOUR_TURN, updatedSecond.getStatus());

      assertNotNull(updatedSecond.getTurnDeadline());

      assertFalse(updatedSecond.getTurnDeadline().isBefore(before.plus(48, ChronoUnit.HOURS)));

      assertFalse(updatedSecond.getTurnDeadline().isAfter(after.plus(48, ChronoUnit.HOURS)));

    } finally {
      if (firstId != null) {
        applicationRepository.deleteById(firstId);
      }

      if (secondId != null) {
        applicationRepository.deleteById(secondId);
      }

      if (animalId != null) {
        animalRepository.deleteById(animalId);
      }
    }
  }

  @Test
  void earlierSubmittedApplicationBlocksQueueAdvancement() {

    Long animalId = null;
    Long firstId = null;
    Long secondId = null;
    Long thirdId = null;

    try {
      Animal animal =
          new Animal(
              "Queue Order Test Dog",
              Species.DOG,
              24,
              AnimalSize.MEDIUM,
              "Animal for queue ordering test",
              true,
              true,
              null,
              Set.of(),
              Instant.now());

      animalId = animalRepository.saveAndFlush(animal).getId();

      Instant submittedAt = Instant.parse("2026-11-01T10:00:00Z");

      Application first =
          new Application(903L, animalId, "Street 1", "Sofia", "Sofia", submittedAt);

      first.setStatus(ApplicationStatus.YOUR_TURN);
      first.setTurnDeadline(Instant.now().plusSeconds(3600));

      firstId = applicationRepository.saveAndFlush(first).getId();

      Application second =
          new Application(
              904L, animalId, "Street 2", "Sofia", "Sofia", submittedAt.plusSeconds(60));

      // Remains SUBMITTED because it has not been validated.
      secondId = applicationRepository.saveAndFlush(second).getId();

      Application third =
          new Application(
              905L, animalId, "Street 3", "Sofia", "Sofia", submittedAt.plusSeconds(120));

      third.setStatus(ApplicationStatus.WAITING);
      thirdId = applicationRepository.saveAndFlush(third).getId();

      // Rejecting the first candidate triggers automatic advancement.
      statusTransitionService.changeStatus(firstId, ApplicationStatus.REJECTED);

      Application updatedFirst = applicationRepository.findById(firstId).orElseThrow();

      Application updatedSecond = applicationRepository.findById(secondId).orElseThrow();

      Application updatedThird = applicationRepository.findById(thirdId).orElseThrow();

      assertEquals(ApplicationStatus.REJECTED, updatedFirst.getStatus());

      assertEquals(ApplicationStatus.SUBMITTED, updatedSecond.getStatus());

      assertEquals(ApplicationStatus.WAITING, updatedThird.getStatus());

      assertNull(updatedThird.getTurnDeadline());

    } finally {
      if (thirdId != null) {
        applicationRepository.deleteById(thirdId);
      }

      if (secondId != null) {
        applicationRepository.deleteById(secondId);
      }

      if (firstId != null) {
        applicationRepository.deleteById(firstId);
      }

      if (animalId != null) {
        animalRepository.deleteById(animalId);
      }
    }
  }
}
