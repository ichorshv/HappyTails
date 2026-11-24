package com.happytails.adoption.controller;

import com.happytails.adoption.dto.ApplicationCreatedResponse;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.ApplicationStatusResponse;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.service.ApplicationStatusService;
import com.happytails.adoption.service.ApplicationSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

  private final ApplicationStatusService applicationStatusService;
  private final ApplicationSubmissionService applicationSubmissionService;

  public ApplicationController(
      ApplicationStatusService applicationStatusService,
      ApplicationSubmissionService applicationSubmissionService) {
    this.applicationStatusService = applicationStatusService;
    this.applicationSubmissionService = applicationSubmissionService;
  }

  /**
   * Submits an application: 201 when it entered the queue, 202 when it is saved as SUBMITTED and
   * its address will be checked later.
   */
  @PostMapping
  public ResponseEntity<ApplicationCreatedResponse> submit(
      @Valid @RequestBody ApplicationRequest request) {
    ApplicationCreatedResponse response = applicationSubmissionService.submit(request);
    HttpStatus status =
        response.status() == ApplicationStatus.SUBMITTED ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
    return ResponseEntity.status(status).body(response);
  }

  @GetMapping("/{id}/status")
  public ApplicationStatusResponse getStatus(@PathVariable Long id) {
    return applicationStatusService.getStatus(id);
  }
}
