package com.happytails.adoption.dto;

import com.happytails.adoption.enums.ApplicationStatus;
import java.time.Instant;

public record ApplicationStatusResponse(
    Long applicationId, ApplicationStatus status, int queuePosition, Instant deadline) {}
