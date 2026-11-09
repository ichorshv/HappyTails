package com.happytails.adoption.applications.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationAddress(
    @NotBlank String street, @NotBlank String city, @NotBlank String municipality) {}
