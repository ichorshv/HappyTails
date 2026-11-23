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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationQueueConcurrencyTest {

  @Autowired private AnimalRepository animalRepository;
  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private ApplicationQueueEntryService queueEntryService;

  @Test
  void concurrentValidationGivesYourTurnToOnlyOneApplicant() throws Exception {

    Animal animal =
        animalRepository.saveAndFlush(
            new Animal(
                "Concurrent Queue Test",
                Species.DOG,
                24,
                AnimalSize.MEDIUM,
                "Concurrency test animal",
                true,
                true,
                null,
                Set.of(),
                Instant.now()));

    Long animalId = animal.getId();
    Long firstId = null;
    Long secondId = null;

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      Instant submittedAt = Instant.parse("2026-11-01T10:00:00Z");

      Application first =
          applicationRepository.saveAndFlush(
              new Application(901L, animalId, "Street 1", "Sofia", "Sofia", submittedAt));

      Application second =
          applicationRepository.saveAndFlush(
              new Application(
                  902L, animalId, "Street 2", "Sofia", "Sofia", submittedAt.plusSeconds(60)));

      firstId = first.getId();
      secondId = second.getId();

      CountDownLatch start = new CountDownLatch(1);

      Long candidateOneId = firstId;
      Long candidateTwoId = secondId;

      Future<?> firstTask =
          executor.submit(
              () -> {
                start.await();
                queueEntryService.onApplicationValidated(candidateOneId);
                return null;
              });

      Future<?> secondTask =
          executor.submit(
              () -> {
                start.await();
                queueEntryService.onApplicationValidated(candidateTwoId);
                return null;
              });

      start.countDown();

      firstTask.get(20, TimeUnit.SECONDS);
      secondTask.get(20, TimeUnit.SECONDS);

      Application updatedFirst = applicationRepository.findById(firstId).orElseThrow();

      Application updatedSecond = applicationRepository.findById(secondId).orElseThrow();

      assertEquals(ApplicationStatus.YOUR_TURN, updatedFirst.getStatus());

      assertEquals(ApplicationStatus.WAITING, updatedSecond.getStatus());

      assertNotNull(updatedFirst.getTurnDeadline());
      assertNull(updatedSecond.getTurnDeadline());

    } finally {
      executor.shutdownNow();

      if (firstId != null) {
        applicationRepository.deleteById(firstId);
      }

      if (secondId != null) {
        applicationRepository.deleteById(secondId);
      }

      animalRepository.deleteById(animalId);
    }
  }
}
