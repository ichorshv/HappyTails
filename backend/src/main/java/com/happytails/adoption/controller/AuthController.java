package com.happytails.adoption.controller;

import com.happytails.adoption.dto.AuthUser;
import com.happytails.adoption.dto.LoginRequest;
import com.happytails.adoption.dto.RegisterRequest;
import com.happytails.adoption.dto.UserResponse;
import com.happytails.adoption.service.CurrentUserService;
import com.happytails.adoption.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final CurrentUserService currentUserService;
  private final SecurityContextRepository securityContextRepository;
  private final CsrfTokenRepository csrfTokenRepository;

  public AuthController(
      UserService userService,
      CurrentUserService currentUserService,
      SecurityContextRepository securityContextRepository,
      CsrfTokenRepository csrfTokenRepository) {
    this.userService = userService;
    this.currentUserService = currentUserService;
    this.securityContextRepository = securityContextRepository;
    this.csrfTokenRepository = csrfTokenRepository;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(
      @Valid @RequestBody RegisterRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    UserResponse user = userService.registerApplicant(body);
    startSession(new AuthUser(user.id(), user.email(), user.role()), request, response);
    return user;
  }

  @PostMapping("/login")
  public UserResponse login(
      @Valid @RequestBody LoginRequest body,
      HttpServletRequest request,
      HttpServletResponse response) {
    AuthUser user = userService.authenticate(body);
    startSession(user, request, response);
    return userService.getUser(user.id());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    new SecurityContextLogoutHandler().logout(request, response, auth);
    renewCsrfToken(request, response);
  }

  @GetMapping("/me")
  public UserResponse me() {
    return userService.getUser(currentUserService.getCurrentUserId());
  }

  private void startSession(
      AuthUser user, HttpServletRequest request, HttpServletResponse response) {
    // A new session ID after login protects against session fixation.
    if (request.getSession(false) != null) {
      request.changeSessionId();
    }
    Authentication auth =
        UsernamePasswordAuthenticationToken.authenticated(
            user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);
    renewCsrfToken(request, response);
  }

  /** After login and logout the CSRF token changes; the new cookie is in the same response. */
  private void renewCsrfToken(HttpServletRequest request, HttpServletResponse response) {
    csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
  }
}
