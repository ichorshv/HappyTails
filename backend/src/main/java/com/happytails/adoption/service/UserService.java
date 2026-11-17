package com.happytails.adoption.service;

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

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
  }

  @Transactional
  public UserResponse registerApplicant(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Паролата е твърде дълга.");
    }
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

  public static String normalizeEmail(String email) {
    return email.strip().toLowerCase(Locale.ROOT);
  }

  private static ApiException emailTaken() {
    return new ApiException(
        HttpStatus.CONFLICT, "AUTH_EMAIL_TAKEN", "Вече има профил с този имейл.");
  }
}
