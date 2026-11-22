package com.happytails.adoption.mapper;

import com.happytails.adoption.dto.AnimalSummaryResponse;
import com.happytails.adoption.entity.Animal;
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
