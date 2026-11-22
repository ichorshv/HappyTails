package com.happytails.adoption.controller;

import com.happytails.adoption.dto.ApplicationStatusResponse;
import com.happytails.adoption.service.ApplicationStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

  private final ApplicationStatusService applicationStatusService;

  public ApplicationController(ApplicationStatusService applicationStatusService) {
    this.applicationStatusService = applicationStatusService;
  }

  @GetMapping("/{id}/status")
  public ApplicationStatusResponse getStatus(@PathVariable Long id) {
    return applicationStatusService.getStatus(id);
  }
}
