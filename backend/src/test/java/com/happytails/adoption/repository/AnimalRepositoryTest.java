package com.happytails.adoption.animals.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.happytails.adoption.animals.api.AnimalSize;
import com.happytails.adoption.animals.api.AnimalStatus;
import com.happytails.adoption.animals.api.RequirementType;
import com.happytails.adoption.animals.api.Species;
import com.happytails.adoption.animals.domain.Animal;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AnimalRepositoryTest {

  @Autowired private AnimalRepository animalRepository;

  @Autowired private EntityManager entityManager;

  @Test
  void seedDataContainsTenAnimalsWithRequirements() {
    assertEquals(10, animalRepository.count());

    Animal mira = animalRepository.findById(1L).orElseThrow();
    assertEquals("Мира", mira.getName());
    assertEquals(AnimalStatus.AVAILABLE, mira.getStatus());
    assertEquals(EnumSet.allOf(RequirementType.class), mira.getRequirements());
  }

  @Test
  void savesNewAnimalAsDraftWithRequirements() {
    Instant createdAt = Instant.parse("2026-11-10T10:00:00Z");
    Animal animal =
        new Animal(
            "Рекс",
            Species.DOG,
            24,
            AnimalSize.MEDIUM,
            "Тестово куче.",
            true,
            false,
            null,
            Set.of(RequirementType.YARD, RequirementType.NO_CATS),
            createdAt);

    Long id = animalRepository.saveAndFlush(animal).getId();
    entityManager.clear();

    Animal loaded = animalRepository.findById(id).orElseThrow();
    assertTrue(loaded.getId() > 10);
    assertEquals(AnimalStatus.DRAFT, loaded.getStatus());
    assertEquals(Species.DOG, loaded.getSpecies());
    assertEquals(Set.of(RequirementType.YARD, RequirementType.NO_CATS), loaded.getRequirements());
    assertEquals(createdAt, loaded.getCreatedAt());
  }
}
