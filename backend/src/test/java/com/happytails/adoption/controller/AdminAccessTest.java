package com.happytails.adoption.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access to /api/admin/**. No admin endpoint exists yet, so a request that passes security ends in
 * 404; a request that is blocked ends in 401 or 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAccessTest {

  private static final String ADMIN_URL = "/api/admin/animals";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("Without login /api/admin/** returns 401 UNAUTHORIZED")
  void anonymousGets401() throws Exception {
    mockMvc
        .perform(get(ADMIN_URL))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  @DisplayName("An applicant gets 403 FORBIDDEN on /api/admin/**")
  void applicantGets403() throws Exception {
    mockMvc
        .perform(get(ADMIN_URL))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }

  @Test
  @WithMockUser(roles = "STAFF")
  @DisplayName("Staff passes the security check on /api/admin/**")
  void staffIsAllowed() throws Exception {
    mockMvc.perform(get(ADMIN_URL)).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("Admin passes the security check on /api/admin/**")
  void adminIsAllowed() throws Exception {
    mockMvc.perform(get(ADMIN_URL)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("A registered applicant with a real session gets 403 on /api/admin/**")
  void registeredApplicantSessionGets403() throws Exception {
    userRepository.save(
        new User(
            "maria@example.com",
            passwordEncoder.encode("Parola123"),
            "Мария Иванова",
            Role.APPLICANT,
            Instant.parse("2026-11-17T10:00:00Z")));
    MockHttpSession session =
        (MockHttpSession)
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"maria@example.com\", \"password\": \"Parola123\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);

    mockMvc
        .perform(get(ADMIN_URL).session(session))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
  }
}
