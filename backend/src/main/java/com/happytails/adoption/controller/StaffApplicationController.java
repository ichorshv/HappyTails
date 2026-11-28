package com.happytails.adoption.controller;

import com.happytails.adoption.dto.StaffRejectApplicationRequest;
import com.happytails.adoption.service.StaffApplicationDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/applications")
public class StaffApplicationController {

  private final StaffApplicationDecisionService decisionService;

  public StaffApplicationController(StaffApplicationDecisionService decisionService) {
    this.decisionService = decisionService;
  }

  @PostMapping("/{id}/approve")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void approve(@PathVariable Long id) {
    decisionService.approve(id);
  }

  @PostMapping("/{id}/reject")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void reject(
      @PathVariable Long id, @Valid @RequestBody StaffRejectApplicationRequest request) {
    decisionService.reject(id, request.reason());
  }

  @PostMapping("/{id}/complete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void complete(@PathVariable Long id) {
    decisionService.complete(id);
  }

  @PostMapping("/{id}/withdraw")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void withdraw(@PathVariable Long id) {
    decisionService.withdraw(id);
  }
}
