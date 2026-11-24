package com.happytails.adoption.controller;

import com.happytails.adoption.dto.ActiveRequest;
import com.happytails.adoption.dto.AdminUserResponse;
import com.happytails.adoption.dto.CreateStaffRequest;
import com.happytails.adoption.dto.PageResponse;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.service.CurrentUserService;
import com.happytails.adoption.service.StaffAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Staff accounts, ADMIN only (see SecurityConfig and docs/api/auth.md, section 5). */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

  private final StaffAccountService staffAccountService;
  private final CurrentUserService currentUserService;

  public AdminUserController(
      StaffAccountService staffAccountService, CurrentUserService currentUserService) {
    this.staffAccountService = staffAccountService;
    this.currentUserService = currentUserService;
  }

  @GetMapping
  public PageResponse<AdminUserResponse> list(
      @RequestParam(required = false) Role role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return staffAccountService.list(role, page, size);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AdminUserResponse create(@Valid @RequestBody CreateStaffRequest body) {
    return staffAccountService.create(body);
  }

  @PutMapping("/{id}/active")
  public AdminUserResponse setActive(
      @PathVariable Long id, @Valid @RequestBody ActiveRequest body) {
    return staffAccountService.setActive(id, body.active(), currentUserService.getCurrentUserId());
  }
}
