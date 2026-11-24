package com.happytails.adoption.dto;

import com.happytails.adoption.enums.ApplicationStatus;

public record ApplicationCreatedResponse(Long id, ApplicationStatus status) {}
