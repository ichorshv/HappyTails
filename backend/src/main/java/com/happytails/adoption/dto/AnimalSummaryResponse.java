package com.happytails.adoption.animals.dto;

import com.happytails.adoption.animals.api.AnimalSize;
import com.happytails.adoption.animals.api.Species;

public record AnimalSummaryResponse(
    Long id,
    String name,
    Species species,
    int ageMonths,
    AnimalSize size,
    boolean vaccinated,
    boolean neutered,
    String photoUrl) {}
