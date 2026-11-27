package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.repository.AnimalRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@RecordApplicationEvents
class AnimalStatusServiceTest {

  private static final long MIRA_AVAILABLE = 1L;
  private static final long BOWIE_RESERVED = 7L;
  private static final long MISHO_DRAFT = 9L;
  private static final long PUHCHO_ADOPTED = 10L;

  @Autowired private AnimalStatusService animalStatusService;

  @Autowired private AnimalRepository animalRepository;

  @Autowired private ApplicationEvents events;

  @Test
  void publishMakesDraftAvailable() {
    animalStatusService.publish(MISHO_DRAFT);

    assertEquals(AnimalStatus.AVAILABLE, statusOf(MISHO_DRAFT));
    assertEquals(
        List.of(new AnimalStatusChanged(MISHO_DRAFT, AnimalStatus.DRAFT, AnimalStatus.AVAILABLE)),
        published());
  }

  @Test
  void markReservedReservesAvailableAnimal() {
    animalStatusService.markReserved(MIRA_AVAILABLE);

    assertEquals(AnimalStatus.RESERVED, statusOf(MIRA_AVAILABLE));
    assertEquals(
        List.of(
            new AnimalStatusChanged(MIRA_AVAILABLE, AnimalStatus.AVAILABLE, AnimalStatus.RESERVED)),
        published());
  }

  @Test
  void markAdoptedAdoptsReservedAnimal() {
    animalStatusService.markAdopted(BOWIE_RESERVED);

    assertEquals(AnimalStatus.ADOPTED, statusOf(BOWIE_RESERVED));
    assertEquals(
        List.of(
            new AnimalStatusChanged(BOWIE_RESERVED, AnimalStatus.RESERVED, AnimalStatus.ADOPTED)),
        published());
  }

  @Test
  void markAvailableReturnsReservedAnimalToTheCatalog() {
    animalStatusService.markAvailable(BOWIE_RESERVED);

    assertEquals(AnimalStatus.AVAILABLE, statusOf(BOWIE_RESERVED));
    assertEquals(
        List.of(
            new AnimalStatusChanged(BOWIE_RESERVED, AnimalStatus.RESERVED, AnimalStatus.AVAILABLE)),
        published());
  }

  @Test
  void rejectsForbiddenTransitionWithoutChangingAnything() {
    ApiException error =
        assertThrows(ApiException.class, () -> animalStatusService.markAdopted(MIRA_AVAILABLE));

    assertEquals(HttpStatus.CONFLICT, error.getStatus());
    assertEquals("ANIMAL_INVALID_STATUS", error.getCode());
    assertEquals(AnimalStatus.AVAILABLE, statusOf(MIRA_AVAILABLE));
    assertEquals(List.of(), published());
  }

  @Test
  void adoptedAnimalCannotBeReservedAgain() {
    ApiException error =
        assertThrows(ApiException.class, () -> animalStatusService.markReserved(PUHCHO_ADOPTED));

    assertEquals("ANIMAL_INVALID_STATUS", error.getCode());
    assertEquals(AnimalStatus.ADOPTED, statusOf(PUHCHO_ADOPTED));
  }

  @Test
  void rejectsUnknownAnimal() {
    ApiException error =
        assertThrows(ApiException.class, () -> animalStatusService.markReserved(999_999L));

    assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
    assertEquals("ANIMAL_NOT_FOUND", error.getCode());
    assertEquals(List.of(), published());
  }

  private AnimalStatus statusOf(long animalId) {
    return animalRepository.findById(animalId).orElseThrow().getStatus();
  }

  private List<AnimalStatusChanged> published() {
    return events.stream(AnimalStatusChanged.class).toList();
  }
}
