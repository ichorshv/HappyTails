package com.happytails.adoption.dto;

import com.happytails.adoption.enums.Role;

/** A user as the admin sees it in the staff accounts screen, with the active flag. */
public record AdminUserResponse(
    Long id, String email, String fullName, Role role, boolean active) {}
