package com.happytails.adoption.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
    @NotNull Long animalId,
    @NotNull @Valid QuestionnaireAnswers answers,
    @NotNull @Valid ApplicationAddress address) {}
