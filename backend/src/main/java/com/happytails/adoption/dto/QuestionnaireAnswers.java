package com.happytails.adoption.dto;

import com.happytails.adoption.enums.HousingType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionnaireAnswers(
    @NotNull HousingType housingType,
    @NotNull Boolean hasYard,
    @NotNull Boolean hasChildrenUnder6,
    @NotNull Boolean hasCats,
    @NotNull Boolean hasDogs,
    @NotNull Boolean hasExperience,
    @NotNull @Min(0) @Max(24) Integer hoursAlonePerDay,
    @NotBlank String motivation) {}
