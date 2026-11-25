package com.happytails.adoption.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record BookWalkRequest(@NotNull Long applicationId, @NotNull Instant startsAt) {}
