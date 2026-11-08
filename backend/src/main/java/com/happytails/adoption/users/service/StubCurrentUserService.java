package com.happytails.adoption.users.service;

import com.happytails.adoption.users.api.CurrentUserService;
import com.happytails.adoption.users.api.Role;
import org.springframework.stereotype.Service;

/** Temporary implementation until real authentication is added (issue #20). */
@Service
public class StubCurrentUserService implements CurrentUserService {

  @Override
  public Long getCurrentUserId() {
    return 1L;
  }

  @Override
  public String getCurrentEmail() {
    return "test.applicant@happytails.local";
  }

  @Override
  public Role getCurrentRole() {
    return Role.APPLICANT;
  }
}
