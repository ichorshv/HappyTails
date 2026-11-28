package com.happytails.adoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffRejectApplicationRequest(
    @NotBlank(message = "Rejection reason is required")
        @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
        String reason) {}
