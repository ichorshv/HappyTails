package com.happytails.adoption.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access rules by role from docs/api. Most of these endpoints are not written yet, so a request
 * that passes security ends in 404 NOT_FOUND; a blocked request ends in 401 or 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccessRulesTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The catalog is open without login")
  void catalogIsPublic() throws Exception {
    mockMvc.perform(get("/api/animals")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("Without login the applicant endpoints return 401")
  void anonymousCannotUseApplicantEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/applications/my"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    mockMvc.perform(get("/api/walks/my")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  @DisplayName("An applicant passes the security check on the applicant endpoints")
  void applicantCanUseApplicantEndpoints() throws Exception {
    mockMvc.perform(get("/api/applications/my")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/walks/my")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  @DisplayName("Staff gets 403 on the applicant endpoints")
  void staffCannotUseApplicantEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/applications/my"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    mockMvc.perform(get("/api/walks/my")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  @DisplayName("An applicant gets 403 on the staff decision endpoint")
  void applicantCannotDecide() throws Exception {
    mockMvc.perform(get("/api/staff/applications/1/decision")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  @DisplayName("Staff passes the security check on the staff decision endpoint")
  void staffCanDecide() throws Exception {
    mockMvc.perform(get("/api/staff/applications/1/decision")).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "STAFF")
  @DisplayName("Staff gets 403 on the admin-only endpoints")
  void staffCannotUseAdminOnlyEndpoints() throws Exception {
    mockMvc
        .perform(get("/api/admin/users"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    mockMvc.perform(get("/api/admin/walk-settings")).andExpect(status().isForbidden());
    mockMvc.perform(get("/api/admin/geo-settings")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("Admin passes the security check on the admin-only endpoints")
  void adminCanUseAdminOnlyEndpoints() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/admin/walk-settings")).andExpect(status().isNotFound());
    mockMvc.perform(get("/api/admin/geo-settings")).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("An address that is not in docs/api is closed: 401 without login")
  void unknownAddressAnonymousGets401() throws Exception {
    mockMvc.perform(get("/api/something-else")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("An address that is not in docs/api is closed: 403 even for admin")
  void unknownAddressAdminGets403() throws Exception {
    mockMvc.perform(get("/api/something-else")).andExpect(status().isForbidden());
  }
}
