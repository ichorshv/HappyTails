package com.happytails.adoption.controller;

import com.happytails.adoption.dto.ApplicationCreatedResponse;
import com.happytails.adoption.dto.ApplicationRequest;
import com.happytails.adoption.dto.ApplicationStatusResponse;
import com.happytails.adoption.service.ApplicationStatusService;
import com.happytails.adoption.service.ApplicationSubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApplicationCreatedResponse submit(@Valid @RequestBody ApplicationRequest request) {
    return applicationSubmissionService.submit(request);
  }

  @GetMapping("/{id}/status")
  public ApplicationStatusResponse getStatus(@PathVariable Long id) {
    return applicationStatusService.getStatus(id);
  }
}
