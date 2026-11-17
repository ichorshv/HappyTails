package com.happytails.adoption.dto;

import com.happytails.adoption.enums.Role;

public record UserResponse(Long id, String email, String fullName, Role role) {}
