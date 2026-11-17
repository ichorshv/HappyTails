package com.happytails.adoption.service;

import com.happytails.adoption.dto.AuthUser;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Reads the logged-in user from the session. In tests with {@code @WithMockUser(username =
 * "<email>")} the user is looked up by email, so it must exist in the database.
 */
@Service
public class SecurityCurrentUserService implements CurrentUserService {

  private final UserService userService;

  public SecurityCurrentUserService(UserService userService) {
    this.userService = userService;
  }

  @Override
  public Long getCurrentUserId() {
    return currentUser().id();
  }

  @Override
  public String getCurrentEmail() {
    return currentUser().email();
  }

  @Override
  public Role getCurrentRole() {
    return currentUser().role();
  }

  private AuthUser currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Не сте влезли в профила си.");
    }
    if (auth.getPrincipal() instanceof AuthUser user) {
      return user;
    }
    return userService.getAuthUserByEmail(auth.getName());
  }
}
