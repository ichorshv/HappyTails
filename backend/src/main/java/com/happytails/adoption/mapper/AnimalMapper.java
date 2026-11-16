package com.happytails.adoption.animals.mapper;

import com.happytails.adoption.animals.domain.Animal;
import com.happytails.adoption.animals.dto.AnimalSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AnimalMapper {

  public AnimalSummaryResponse toSummary(Animal animal) {
    return new AnimalSummaryResponse(
        animal.getId(),
        animal.getName(),
        animal.getSpecies(),
        animal.getAgeMonths(),
        animal.getSize(),
        animal.isVaccinated(),
        animal.isNeutered(),
        null);
  }
}
