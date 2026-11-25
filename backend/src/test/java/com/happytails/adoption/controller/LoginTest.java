package com.happytails.adoption.controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginTest {

  private static final String EMAIL = "staff@happytails.bg";
  private static final String PASSWORD = "Parola123";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private User staff;

  @BeforeEach
  void createUser() {
    staff =
        userRepository.save(
            new User(
                EMAIL,
                passwordEncoder.encode(PASSWORD),
                "Иван Петров",
                Role.STAFF,
                Instant.parse("2026-11-17T10:00:00Z")));
  }

  private MockHttpServletRequestBuilder loginRequest(String email, String password) {
    return post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(
            """
            {"email": "%s", "password": "%s"}
            """
                .formatted(email, password));
  }

  private ResultActions login(String email, String password) throws Exception {
    return mockMvc.perform(loginRequest(email, password).with(csrf()));
  }

  private MockHttpSession loginAndGetSession() throws Exception {
    MvcResult result = login(EMAIL, PASSWORD).andExpect(status().isOk()).andReturn();
    return (MockHttpSession) result.getRequest().getSession(false);
  }

  @Test
  @DisplayName("Login with a correct password starts a session for /me")
  void loginStartsSession() throws Exception {
    login("Staff@HappyTails.bg", PASSWORD)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(EMAIL))
        .andExpect(jsonPath("$.role").value("STAFF"))
        .andExpect(cookie().exists("XSRF-TOKEN"));

    MockHttpSession session = loginAndGetSession();
    mockMvc
        .perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(staff.getId()))
        .andExpect(jsonPath("$.fullName").value("Иван Петров"));
  }

  @Test
  @DisplayName("Wrong password and unknown email give the same 401")
  void badCredentialsAreRejected() throws Exception {
    login(EMAIL, "Greshna123")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"));
    login("nobody@happytails.bg", PASSWORD)
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"));
  }

  @Test
  @DisplayName("A deactivated account cannot log in")
  void disabledAccountIsRejected() throws Exception {
    staff.setActive(false);
    userRepository.flush();

    login(EMAIL, PASSWORD)
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("AUTH_ACCOUNT_DISABLED"));
    login(EMAIL, "Greshna123")
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_BAD_CREDENTIALS"));
  }

  @Test
  @DisplayName("Login without a CSRF token is refused")
  void loginNeedsCsrfToken() throws Exception {
    mockMvc.perform(loginRequest(EMAIL, PASSWORD)).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Login changes the session ID")
  void loginChangesSessionId() throws Exception {
    MockHttpSession before = new MockHttpSession();
    String oldId = before.getId();
    MvcResult result =
        mockMvc
            .perform(loginRequest(EMAIL, PASSWORD).with(csrf()).session(before))
            .andExpect(status().isOk())
            .andReturn();
    assertNotEquals(oldId, result.getRequest().getSession(false).getId());
  }

  @Test
  @DisplayName("Logout ends the session")
  void logoutEndsSession() throws Exception {
    MockHttpSession session = loginAndGetSession();

    mockMvc
        .perform(post("/api/auth/logout").with(csrf()).session(session))
        .andExpect(status().isNoContent())
        .andExpect(cookie().exists("XSRF-TOKEN"));

    mockMvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Registration also logs the new applicant in")
  void registerLogsIn() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email": "maria@example.com", "password": "Parola123",
                         "fullName": "Мария Иванова", "city": "София", "municipality": "Столична"}
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

    mockMvc
        .perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("maria@example.com"))
        .andExpect(jsonPath("$.role").value("APPLICANT"));
  }
}
