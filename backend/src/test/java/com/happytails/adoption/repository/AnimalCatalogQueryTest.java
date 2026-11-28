package com.happytails.adoption.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AgeGroup;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.enums.Species;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AnimalCatalogQueryTest {

  @Autowired private AnimalRepository animalRepository;

  @Test
  void ageGroupsIncludeTheirBoundaryMonths() {
    saveAvailableLargeCat("Котка 11", 11);
    saveAvailableLargeCat("Котка 12", 12);
    saveAvailableLargeCat("Котка 35", 35);
    saveAvailableLargeCat("Котка 36", 36);

    assertEquals(Set.of("Котка 11"), largeCatsIn(AgeGroup.PUPPY));
    assertEquals(Set.of("Котка 12", "Котка 35"), largeCatsIn(AgeGroup.YOUNG));
    assertEquals(Set.of("Котка 36"), largeCatsIn(AgeGroup.ADULT));
  }

  @Test
  void draftAnimalsAreNotInTheCatalog() {
    Animal draft =
        new Animal(
            "Чернова",
            Species.CAT,
            12,
            AnimalSize.LARGE,
            "Тестова котка.",
            true,
            true,
            null,
            Set.of(),
            Instant.parse("2026-11-16T10:00:00Z"));
    animalRepository.save(draft);

    assertEquals(Set.of(), largeCatsIn(AgeGroup.YOUNG));
  }

  private Set<String> largeCatsIn(AgeGroup age) {
    return animalRepository
        .findCatalog(
            AnimalStatus.AVAILABLE,
            Species.CAT,
            AnimalSize.LARGE,
            age.getMinMonths(),
            age.getMaxMonths(),
            PageRequest.of(0, 12))
        .stream()
        .map(Animal::getName)
        .collect(Collectors.toSet());
  }

  private void saveAvailableLargeCat(String name, int ageMonths) {
    Animal animal =
        new Animal(
            name,
            Species.CAT,
            ageMonths,
            AnimalSize.LARGE,
            "Тестова котка.",
            true,
            true,
            null,
            Set.of(),
            Instant.parse("2026-11-16T10:00:00Z"));
    ReflectionTestUtils.setField(animal, "status", AnimalStatus.AVAILABLE);
    animalRepository.save(animal);
  }
}
