package com.happytails.adoption.dto;

import com.happytails.adoption.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** A new staff or admin account. The fields follow the same rules as {@link RegisterRequest}. */
public record CreateStaffRequest(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(
            regexp = "^(?=.*\\p{L})(?=.*\\d).*$",
            message = "трябва да съдържа поне една буква и една цифра")
        String password,
    @NotBlank @Size(min = 2, max = 100) String fullName,
    @NotNull Role role) {}
