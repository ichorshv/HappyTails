package com.happytails.adoption.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * POST /api/applications from end to end: security, validation, business rules and the database.
 * Seed data: animals 1-6 and 8 are AVAILABLE, animal 7 is RESERVED.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationSubmissionControllerTest {

  private static final String EMAIL = "applicant@example.com";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ApplicationRepository applicationRepository;

  private User createUser(String email, Role role) {
    return userRepository.save(
        new User(
            email, "test-password-hash", "Test User", role, Instant.parse("2026-11-01T10:00:00Z")));
  }

  private void createActiveApplication(Long applicantId, Long animalId) {
    Application application =
        new Application(
            applicantId,
            animalId,
            "Test Street",
            "Sofia",
            "Sofia",
            Instant.parse("2026-11-01T10:00:00Z"));
    application.setStatus(ApplicationStatus.WAITING);
    applicationRepository.save(application);
  }

  private ResultActions submit(String body) throws Exception {
    return mockMvc.perform(
        post("/api/applications")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  private String validBody(long animalId) {
    return """
        {
          "animalId": %d,
          "answers": {
            "housingType": "HOUSE",
            "hasYard": true,
            "hasChildrenUnder6": false,
            "hasCats": false,
            "hasDogs": false,
            "hasExperience": true,
            "hoursAlonePerDay": 4,
            "motivation": "Искаме да дадем дом."
          },
          "address": {
            "street": "ул. Витоша 10",
            "city": "София",
            "municipality": "Столична"
          }
        }
        """
        .formatted(animalId);
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("A valid application is created and put in the queue (201, WAITING)")
  void createsApplication() throws Exception {
    createUser(EMAIL, Role.APPLICANT);

    submit(validBody(1))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.status").value("WAITING"));
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("An incomplete application is rejected with 400 VALIDATION_ERROR")
  void rejectsIncompleteApplication() throws Exception {
    createUser(EMAIL, Role.APPLICANT);

    submit(validBody(1).replace("\"Искаме да дадем дом.\"", "\"\""))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("A second application for the same animal is rejected with 409")
  void rejectsDuplicateApplication() throws Exception {
    createUser(EMAIL, Role.APPLICANT);

    submit(validBody(1)).andExpect(status().isCreated());

    submit(validBody(1))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("APPLICATION_DUPLICATE"));
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("A fourth active application is rejected with 422")
  void rejectsWhenLimitReached() throws Exception {
    User applicant = createUser(EMAIL, Role.APPLICANT);
    createActiveApplication(applicant.getId(), 2L);
    createActiveApplication(applicant.getId(), 3L);
    createActiveApplication(applicant.getId(), 4L);

    submit(validBody(1))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("APPLICATION_LIMIT_REACHED"));
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("An animal that is not available (RESERVED) returns 404")
  void rejectsUnavailableAnimal() throws Exception {
    createUser(EMAIL, Role.APPLICANT);

    submit(validBody(7))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ANIMAL_NOT_AVAILABLE"));
  }

  @Test
  @DisplayName("Without login the endpoint returns 401")
  void anonymousGets401() throws Exception {
    submit(validBody(1))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @WithMockUser(username = "staff@example.com", roles = "STAFF")
  @DisplayName("Staff cannot submit applications (403)")
  void staffGets403() throws Exception {
    createUser("staff@example.com", Role.STAFF);

    submit(validBody(1)).andExpect(status().isForbidden());
  }
}
