package com.happytails.adoption.dto;

import com.happytails.adoption.enums.Role;

/** The logged-in user. {@code city} and {@code municipality} are null for staff accounts. */
public record UserResponse(
    Long id, String email, String fullName, Role role, String city, String municipality) {}
