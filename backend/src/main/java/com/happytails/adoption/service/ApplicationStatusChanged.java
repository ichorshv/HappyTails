package com.happytails.adoption.service;

import com.happytails.adoption.enums.ApplicationStatus;

public record ApplicationStatusChanged(
    Long applicationId, Long animalId, ApplicationStatus oldStatus, ApplicationStatus newStatus) {}
