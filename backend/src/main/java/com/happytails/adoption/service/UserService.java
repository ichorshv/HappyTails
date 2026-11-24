package com.happytails.adoption.service;

import com.happytails.adoption.dto.AuthUser;
import com.happytails.adoption.dto.LoginRequest;
import com.happytails.adoption.dto.RegisterRequest;
import com.happytails.adoption.dto.UserResponse;
import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.mapper.UserMapper;
import com.happytails.adoption.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  /** BCrypt uses at most 72 bytes; a Cyrillic letter takes 2 bytes. */
  private static final int MAX_PASSWORD_BYTES = 72;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;

  /** Checked when the email is unknown, so the answer takes as long as for a real user. */
  private final String dummyHash;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.dummyHash = passwordEncoder.encode("dummy-password-1");
  }

  @Transactional
  public UserResponse registerApplicant(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    checkPasswordLength(request.password());
    if (userRepository.existsByEmail(email)) {
      throw emailTaken();
    }
    User user =
        new User(
            email,
            passwordEncoder.encode(request.password()),
            request.fullName().strip(),
            Role.APPLICANT,
            clock.instant());
    try {
      return UserMapper.toResponse(userRepository.saveAndFlush(user));
    } catch (DataIntegrityViolationException ex) {
      // Two registrations with the same email at the same time: the unique constraint wins.
      throw emailTaken();
    }
  }

  /**
   * Checks the email and password. The error does not say which of the two is wrong. The password
   * is checked before the active flag, so only the owner can learn that an account is disabled.
   */
  @Transactional(readOnly = true)
  public AuthUser authenticate(LoginRequest request) {
    User user = userRepository.findByEmail(normalizeEmail(request.email())).orElse(null);
    String hash = user != null ? user.getPasswordHash() : dummyHash;
    boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
    if (user == null || !passwordMatches) {
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "AUTH_BAD_CREDENTIALS", "Грешен имейл или парола.");
    }
    if (!user.isActive()) {
      throw new ApiException(
          HttpStatus.FORBIDDEN, "AUTH_ACCOUNT_DISABLED", "Профилът е деактивиран.");
    }
    return toAuthUser(user);
  }

  @Transactional(readOnly = true)
  public UserResponse getUser(Long id) {
    return userRepository
        .findById(id)
        .map(UserMapper::toResponse)
        .orElseThrow(() -> unauthorized());
  }

  @Transactional(readOnly = true)
  public AuthUser getAuthUserByEmail(String email) {
    return userRepository
        .findByEmail(normalizeEmail(email))
        .map(UserService::toAuthUser)
        .orElseThrow(() -> unauthorized());
  }

  public static AuthUser toAuthUser(User user) {
    return new AuthUser(user.getId(), user.getEmail(), user.getRole());
  }

  static void checkPasswordLength(String password) {
    if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Паролата е твърде дълга.");
    }
  }

  public static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }

  private static ApiException unauthorized() {
    return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Не сте влезли в профила си.");
  }

  static ApiException emailTaken() {
    return new ApiException(
        HttpStatus.CONFLICT, "AUTH_EMAIL_TAKEN", "Вече има профил с този имейл.");
  }
}
