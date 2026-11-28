package com.happytails.adoption.service;

import com.happytails.adoption.dto.AdminUserResponse;
import com.happytails.adoption.dto.CreateStaffRequest;
import com.happytails.adoption.dto.PageResponse;
import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.mapper.UserMapper;
import com.happytails.adoption.repository.UserRepository;
import java.time.Clock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Staff accounts managed by the admin (docs/api/auth.md, section 5). */
@Service
public class StaffAccountService {

  private static final int MAX_PAGE_SIZE = 100;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final SessionTerminator sessionTerminator;
  private final Clock clock;

  public StaffAccountService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      SessionTerminator sessionTerminator,
      Clock clock) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.sessionTerminator = sessionTerminator;
    this.clock = clock;
  }

  /** All users, or only those with the given role, sorted by id. */
  @Transactional(readOnly = true)
  public PageResponse<AdminUserResponse> list(Role role, int page, int size) {
    if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "VALIDATION_ERROR",
          "Страницата трябва да е 0 или повече, а размерът от 1 до " + MAX_PAGE_SIZE + ".");
    }
    Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
    Page<User> users =
        role == null ? userRepository.findAll(pageable) : userRepository.findByRole(role, pageable);
    return new PageResponse<>(
        users.getContent().stream().map(UserMapper::toAdminResponse).toList(),
        users.getNumber(),
        users.getTotalElements(),
        users.getTotalPages());
  }

  /** Creates a STAFF or ADMIN account. Applicants register themselves. */
  @Transactional
  public AdminUserResponse create(CreateStaffRequest request) {
    if (request.role() == Role.APPLICANT) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Ролята трябва да е STAFF или ADMIN.");
    }
    UserService.checkPasswordLength(request.password());
    String email = UserService.normalizeEmail(request.email());
    if (userRepository.existsByEmail(email)) {
      throw UserService.emailTaken();
    }
    User user =
        new User(
            email,
            passwordEncoder.encode(request.password()),
            request.fullName().strip(),
            request.role(),
            clock.instant());
    try {
      return UserMapper.toAdminResponse(userRepository.saveAndFlush(user));
    } catch (DataIntegrityViolationException ex) {
      throw UserService.emailTaken();
    }
  }

  /**
   * Turns an account on or off. A deactivated user loses all sessions at once, so the next request
   * returns 401 and a new login returns AUTH_ACCOUNT_DISABLED. The admin cannot deactivate
   * themselves, so the system always keeps at least the admin who is working in it.
   */
  @Transactional
  public AdminUserResponse setActive(Long id, boolean active, Long currentUserId) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Потребителят не съществува."));
    if (!active && id.equals(currentUserId)) {
      throw new ApiException(
          HttpStatus.CONFLICT,
          "USER_CANNOT_DEACTIVATE_SELF",
          "Не можете да деактивирате собствения си профил.");
    }
    user.setActive(active);
    if (!active) {
      sessionTerminator.endAllSessions(id);
    }
    return UserMapper.toAdminResponse(user);
  }
}
