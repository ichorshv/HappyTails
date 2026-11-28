package com.happytails.adoption.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Staff accounts managed by the admin (docs/api/auth.md, section 5). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTest {

  private static final String PASSWORD = "Parola123";
  private static final String ADMIN_EMAIL = "admin@happytails.bg";
  private static final String STAFF_EMAIL = "staff@happytails.bg";
  private static final String APPLICANT_EMAIL = "maria@example.com";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private User admin;
  private User staff;

  @BeforeEach
  void createUsers() {
    admin = createUser(ADMIN_EMAIL, "Админ Админов", Role.ADMIN);
    staff = createUser(STAFF_EMAIL, "Иван Петров", Role.STAFF);
    createUser(APPLICANT_EMAIL, "Мария Иванова", Role.APPLICANT);
  }

  private User createUser(String email, String fullName, Role role) {
    return userRepository.save(
        new User(
            email,
            passwordEncoder.encode(PASSWORD),
            fullName,
            role,
            Instant.parse("2026-11-24T10:00:00Z")));
  }

  private ResultActions loginAs(String email) throws Exception {
    return mockMvc.perform(
        post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                {"email": "%s", "password": "%s"}
                """
                    .formatted(email, PASSWORD)));
  }

  private MockHttpSession sessionOf(String email) throws Exception {
    return (MockHttpSession)
        loginAs(email).andExpect(status().isOk()).andReturn().getRequest().getSession(false);
  }

  private ResultActions createStaff(MockHttpSession session, String body) throws Exception {
    return mockMvc.perform(
        post("/api/admin/users")
            .session(session)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private ResultActions setActive(MockHttpSession session, Long id, boolean active)
      throws Exception {
    return mockMvc.perform(
        put("/api/admin/users/{id}/active", id)
            .session(session)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"active\": %s}".formatted(active)));
  }

  @Test
  @DisplayName("Without login the staff accounts return 401")
  void anonymousGets401() throws Exception {
    mockMvc
        .perform(get("/api/admin/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("Staff cannot manage staff accounts: 403")
  void staffGets403() throws Exception {
    MockHttpSession session = sessionOf(STAFF_EMAIL);

    mockMvc
        .perform(get("/api/admin/users").session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    createStaff(
            session,
            """
            {"email": "new@happytails.bg", "password": "Parola123",
             "fullName": "Нов Служител", "role": "STAFF"}
            """)
        .andExpect(status().isForbidden());
    setActive(session, admin.getId(), false).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("The admin creates a staff account that can log in")
  void adminCreatesStaff() throws Exception {
    createStaff(
            sessionOf(ADMIN_EMAIL),
            """
            {"email": "New@HappyTails.bg", "password": "Parola123",
             "fullName": " Нов Служител ", "role": "STAFF"}
            """)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("new@happytails.bg"))
        .andExpect(jsonPath("$.fullName").value("Нов Служител"))
        .andExpect(jsonPath("$.role").value("STAFF"))
        .andExpect(jsonPath("$.active").value(true));

    loginAs("new@happytails.bg").andExpect(status().isOk());
  }

  @Test
  @DisplayName("Creating an account with a taken email returns 409 AUTH_EMAIL_TAKEN")
  void takenEmailIsRejected() throws Exception {
    createStaff(
            sessionOf(ADMIN_EMAIL),
            """
            {"email": "Staff@happytails.bg", "password": "Parola123",
             "fullName": "Друг Служител", "role": "STAFF"}
            """)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("AUTH_EMAIL_TAKEN"));
  }

  @Test
  @DisplayName("The admin cannot create an applicant, and the fields are checked: 400")
  void invalidStaffIsRejected() throws Exception {
    MockHttpSession session = sessionOf(ADMIN_EMAIL);

    createStaff(
            session,
            """
            {"email": "new@happytails.bg", "password": "Parola123",
             "fullName": "Нов Служител", "role": "APPLICANT"}
            """)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    createStaff(
            session,
            """
            {"email": "not-an-email", "password": "short",
             "fullName": "Нов Служител", "role": "STAFF"}
            """)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @DisplayName("The list can be filtered by role")
  void listFiltersByRole() throws Exception {
    MockHttpSession session = sessionOf(ADMIN_EMAIL);

    mockMvc
        .perform(get("/api/admin/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].email", hasItem(APPLICANT_EMAIL)))
        .andExpect(jsonPath("$.content[*].email", hasItem(STAFF_EMAIL)));

    mockMvc
        .perform(get("/api/admin/users").param("role", "STAFF").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].role", everyItem(is("STAFF"))))
        .andExpect(jsonPath("$.content[*].email", hasItem(STAFF_EMAIL)))
        .andExpect(jsonPath("$.content[*].email", not(hasItem(APPLICANT_EMAIL))));
  }

  @Test
  @DisplayName("A wrong role or page size in the list returns 400")
  void invalidListParametersAreRejected() throws Exception {
    MockHttpSession session = sessionOf(ADMIN_EMAIL);

    mockMvc
        .perform(get("/api/admin/users").param("role", "BOSS").session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    mockMvc
        .perform(get("/api/admin/users").param("size", "0").session(session))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @DisplayName("A deactivated user is logged out at once and cannot log in again")
  void deactivationEndsSessions() throws Exception {
    MockHttpSession staffSession = sessionOf(STAFF_EMAIL);
    mockMvc.perform(get("/api/auth/me").session(staffSession)).andExpect(status().isOk());

    setActive(sessionOf(ADMIN_EMAIL), staff.getId(), false)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(staff.getId()))
        .andExpect(jsonPath("$.active").value(false));

    mockMvc
        .perform(get("/api/auth/me").session(staffSession))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    loginAs(STAFF_EMAIL)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_DISABLED"));
  }

  @Test
  @DisplayName("A reactivated user can log in again")
  void reactivationAllowsLogin() throws Exception {
    MockHttpSession adminSession = sessionOf(ADMIN_EMAIL);
    setActive(adminSession, staff.getId(), false).andExpect(status().isOk());

    setActive(adminSession, staff.getId(), true)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(true));

    loginAs(STAFF_EMAIL).andExpect(status().isOk());
  }

  @Test
  @DisplayName("The admin cannot deactivate themselves: 409")
  void adminCannotDeactivateSelf() throws Exception {
    MockHttpSession session = sessionOf(ADMIN_EMAIL);

    setActive(session, admin.getId(), false)
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("USER_CANNOT_DEACTIVATE_SELF"));
    mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk());
  }

  @Test
  @DisplayName("An unknown user returns 404 USER_NOT_FOUND")
  void unknownUserIs404() throws Exception {
    setActive(sessionOf(ADMIN_EMAIL), 999_999L, false)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
  }
}
