package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.*;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.Species;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ApplicationQueueEntryIntegrationTest {

  @Autowired private AnimalRepository animalRepository;
  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private ApplicationQueueEntryService queueEntryService;
  @Autowired private QueueService queueService;
  @Autowired private EntityManager entityManager;

  private Long createAnimal() {
    Animal animal =
        new Animal(
            "Queue Test Dog",
            Species.DOG,
            24,
            AnimalSize.MEDIUM,
            "Animal for queue integration testing",
            true,
            true,
            null,
            Set.of(),
            Instant.now());

    return animalRepository.saveAndFlush(animal).getId();
  }

  private Long createApplication(Long applicantId, Long animalId, Instant submittedAt) {

    Application application =
        new Application(applicantId, animalId, "Test Street", "Sofia", "Sofia", submittedAt);

    return applicationRepository.saveAndFlush(application).getId();
  }

  @Test
  void threeApplicantsArePlacedInCorrectQueueOrder() {
    Long animalId = createAnimal();

    Instant submittedAt = Instant.parse("2026-11-01T10:00:00Z");

    Long firstId = createApplication(101L, animalId, submittedAt);

    Long secondId = createApplication(102L, animalId, submittedAt.plusSeconds(60));

    Long thirdId = createApplication(103L, animalId, submittedAt.plusSeconds(120));

    Instant before = Instant.now();

    queueEntryService.onApplicationValidated(firstId);
    queueEntryService.onApplicationValidated(secondId);
    queueEntryService.onApplicationValidated(thirdId);

    Instant after = Instant.now();

    entityManager.flush();
    entityManager.clear();

    Application first = applicationRepository.findById(firstId).orElseThrow();

    Application second = applicationRepository.findById(secondId).orElseThrow();

    Application third = applicationRepository.findById(thirdId).orElseThrow();

    assertEquals(ApplicationStatus.YOUR_TURN, first.getStatus());
    assertEquals(ApplicationStatus.WAITING, second.getStatus());
    assertEquals(ApplicationStatus.WAITING, third.getStatus());

    assertNotNull(first.getTurnDeadline());

    assertFalse(first.getTurnDeadline().isBefore(before.plus(48, ChronoUnit.HOURS)));

    assertFalse(first.getTurnDeadline().isAfter(after.plus(48, ChronoUnit.HOURS)));

    assertNull(second.getTurnDeadline());
    assertNull(third.getTurnDeadline());

    assertEquals(1, queueService.getQueuePosition(firstId));
    assertEquals(2, queueService.getQueuePosition(secondId));
    assertEquals(3, queueService.getQueuePosition(thirdId));
  }

  @Test
  void earlierSubmittedApplicationKeepsLaterOneWaiting() {
    Long animalId = createAnimal();

    Instant submittedAt = Instant.parse("2026-11-01T10:00:00Z");

    Long firstId = createApplication(201L, animalId, submittedAt);

    Long secondId = createApplication(202L, animalId, submittedAt.plusSeconds(60));

    queueEntryService.onApplicationValidated(secondId);

    Application second = applicationRepository.findById(secondId).orElseThrow();

    assertEquals(ApplicationStatus.WAITING, second.getStatus());
    assertNull(second.getTurnDeadline());

    queueEntryService.onApplicationValidated(firstId);

    entityManager.flush();
    entityManager.clear();

    Application first = applicationRepository.findById(firstId).orElseThrow();

    second = applicationRepository.findById(secondId).orElseThrow();

    assertEquals(ApplicationStatus.YOUR_TURN, first.getStatus());
    assertNotNull(first.getTurnDeadline());

    assertEquals(ApplicationStatus.WAITING, second.getStatus());
    assertEquals(1, queueService.getQueuePosition(firstId));
    assertEquals(2, queueService.getQueuePosition(secondId));
  }
}
