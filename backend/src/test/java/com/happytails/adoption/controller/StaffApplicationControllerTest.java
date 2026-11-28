package com.happytails.adoption.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.repository.AnimalRepository;
import com.happytails.adoption.repository.ApplicationRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StaffApplicationControllerTest {

  private static final String APPROVE_URL = "/api/staff/applications/1/approve";

  @Autowired private MockMvc mockMvc;

  @Autowired private ApplicationRepository applicationRepository;

  @Autowired private AnimalRepository animalRepository;

  @Test
  void anonymousUserCannotApproveApplication() throws Exception {
    mockMvc.perform(post(APPROVE_URL).with(csrf())).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  void applicantCannotApproveApplication() throws Exception {
    mockMvc.perform(post(APPROVE_URL).with(csrf())).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void staffWithoutCsrfCannotApproveApplication() throws Exception {
    mockMvc.perform(post(APPROVE_URL)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void staffCanAccessApproveEndpoint() throws Exception {
    mockMvc
        .perform(post("/api/staff/applications/99999999/approve").with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanAccessApproveEndpoint() throws Exception {
    mockMvc
        .perform(post("/api/staff/applications/99999999/approve").with(csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void approvingApplicationReservesAnimalInDatabase() throws Exception {

    // Animal 1 is AVAILABLE in the existing seed data.
    Long animalId = 1L;

    assertEquals(
        AnimalStatus.AVAILABLE, animalRepository.findById(animalId).orElseThrow().getStatus());

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.WALK_SCHEDULED);

    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/approve").with(csrf()))
        .andExpect(status().isNoContent());

    Application updated = applicationRepository.findById(applicationId).orElseThrow();

    assertEquals(ApplicationStatus.APPROVED, updated.getStatus());

    assertEquals(
        AnimalStatus.RESERVED, animalRepository.findById(animalId).orElseThrow().getStatus());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void completingApplicationMarksAnimalAdoptedInDatabase() throws Exception {

    Long animalId = 1L;

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.WALK_SCHEDULED);
    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    // First approve the application.
    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/approve").with(csrf()))
        .andExpect(status().isNoContent());

    assertEquals(
        AnimalStatus.RESERVED, animalRepository.findById(animalId).orElseThrow().getStatus());

    // Then complete the adoption.
    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/complete").with(csrf()))
        .andExpect(status().isNoContent());

    Application updated = applicationRepository.findById(applicationId).orElseThrow();

    assertEquals(ApplicationStatus.COMPLETED, updated.getStatus());

    assertEquals(
        AnimalStatus.ADOPTED, animalRepository.findById(animalId).orElseThrow().getStatus());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void rejectingApplicationSavesReasonInDatabase() throws Exception {

    Long animalId = 1L;

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.WALK_SCHEDULED);
    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    mockMvc
        .perform(
            post("/api/staff/applications/" + applicationId + "/reject")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                  {
                    "reason": "Applicant does not meet requirements"
                  }
                  """))
        .andExpect(status().isNoContent());

    Application updated = applicationRepository.findById(applicationId).orElseThrow();

    assertEquals(ApplicationStatus.REJECTED, updated.getStatus());

    assertEquals("Applicant does not meet requirements", updated.getRejectionReason());

    assertEquals(
        AnimalStatus.AVAILABLE, animalRepository.findById(animalId).orElseThrow().getStatus());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void withdrawingApprovedApplicationMakesAnimalAvailableInDatabase() throws Exception {

    Long animalId = 1L;

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.WALK_SCHEDULED);
    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    // Approve the application first.
    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/approve").with(csrf()))
        .andExpect(status().isNoContent());

    assertEquals(
        AnimalStatus.RESERVED, animalRepository.findById(animalId).orElseThrow().getStatus());

    // Withdraw the approved application.
    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/withdraw").with(csrf()))
        .andExpect(status().isNoContent());

    Application updated = applicationRepository.findById(applicationId).orElseThrow();

    assertEquals(ApplicationStatus.WITHDRAWN, updated.getStatus());

    assertEquals(
        AnimalStatus.AVAILABLE, animalRepository.findById(animalId).orElseThrow().getStatus());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  void cannotApproveCompletedApplication() throws Exception {

    Long animalId = 1L;

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.COMPLETED);

    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    mockMvc
        .perform(post("/api/staff/applications/" + applicationId + "/approve").with(csrf()))
        .andExpect(status().isConflict());

    Application updated = applicationRepository.findById(applicationId).orElseThrow();

    assertEquals(ApplicationStatus.COMPLETED, updated.getStatus());

    assertEquals(
        AnimalStatus.AVAILABLE, animalRepository.findById(animalId).orElseThrow().getStatus());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void failedAnimalReservationRollsBackApplicationApproval() throws Exception {

    // Animal 7 is RESERVED in the existing seed data.
    Long animalId = 7L;

    assertEquals(
        AnimalStatus.RESERVED, animalRepository.findById(animalId).orElseThrow().getStatus());

    Application application =
        new Application(
            10L, animalId, "Test Street", "Sofia", "Sofia", Instant.parse("2026-11-28T10:00:00Z"));

    application.setStatus(ApplicationStatus.WALK_SCHEDULED);
    application = applicationRepository.saveAndFlush(application);

    Long applicationId = application.getId();

    try {
      mockMvc
          .perform(post("/api/staff/applications/" + applicationId + "/approve").with(csrf()))
          .andExpect(status().isConflict());

      // Verify the application was NOT approved.
      Application updated = applicationRepository.findById(applicationId).orElseThrow();

      assertEquals(ApplicationStatus.WALK_SCHEDULED, updated.getStatus());

      // The animal must remain RESERVED.
      assertEquals(
          AnimalStatus.RESERVED, animalRepository.findById(animalId).orElseThrow().getStatus());

    } finally {
      applicationRepository.deleteById(applicationId);
    }
  }
}
