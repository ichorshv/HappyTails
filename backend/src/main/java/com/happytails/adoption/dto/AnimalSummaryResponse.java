package com.happytails.adoption.dto;

import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.Species;

public record AnimalSummaryResponse(
    Long id,
    String name,
    Species species,
    int ageMonths,
    AnimalSize size,
    boolean vaccinated,
    boolean neutered,
    String photoUrl) {}
