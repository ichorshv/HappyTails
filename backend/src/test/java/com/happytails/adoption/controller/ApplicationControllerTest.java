package com.happytails.adoption.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ApplicationRepository applicationRepository;
  @Autowired private UserRepository userRepository;

  private User createUser(String email) {
    return userRepository.save(
        new User(
            email,
            "test-password-hash",
            "Test User",
            Role.APPLICANT,
            Instant.parse("2026-11-01T10:00:00Z")));
  }

  private Application createApplication(Long applicantId, ApplicationStatus status) {
    Application application =
        new Application(
            applicantId,
            100L,
            "Test Street",
            "Sofia",
            "Sofia",
            Instant.parse("2026-11-01T10:00:00Z"));

    application.setStatus(status);
    return applicationRepository.save(application);
  }

  @Test
  void anonymousUserCannotViewApplicationStatus() throws Exception {
    mockMvc
        .perform(get("/api/applications/1/status"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @WithMockUser(username = "owner@example.com", roles = "APPLICANT")
  void ownerCanViewApplicationStatus() throws Exception {
    User owner = createUser("owner@example.com");
    Application application = createApplication(owner.getId(), ApplicationStatus.WAITING);

    mockMvc
        .perform(get("/api/applications/" + application.getId() + "/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.applicationId").value(application.getId()))
        .andExpect(jsonPath("$.status").value("WAITING"))
        .andExpect(jsonPath("$.queuePosition").value(1))
        .andExpect(jsonPath("$.deadline").isEmpty());
  }

  @Test
  @WithMockUser(username = "viewer@example.com", roles = "APPLICANT")
  void applicantCannotViewSomeoneElsesApplication() throws Exception {
    createUser("viewer@example.com");
    User owner = createUser("owner@example.com");

    Application application = createApplication(owner.getId(), ApplicationStatus.WAITING);

    mockMvc
        .perform(get("/api/applications/" + application.getId() + "/status"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("APPLICATION_ACCESS_DENIED"));
  }

  @Test
  @WithMockUser(username = "owner@example.com", roles = "APPLICANT")
  void missingApplicationReturns404() throws Exception {
    createUser("owner@example.com");

    mockMvc
        .perform(get("/api/applications/999999999/status"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("APPLICATION_NOT_FOUND"));
  }
}
