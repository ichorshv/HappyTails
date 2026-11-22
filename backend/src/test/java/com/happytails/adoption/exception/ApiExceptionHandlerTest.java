package com.happytails.adoption.exception;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Every error comes back in the common format {status, code, message, details}. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser(roles = "STAFF")
  @DisplayName("A missing address returns 404 NOT_FOUND in the common format")
  void missingAddressReturns404() throws Exception {
    mockMvc
        .perform(get("/api/admin/does-not-exist"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").isNotEmpty())
        .andExpect(jsonPath("$.details").isArray());
  }

  @Test
  @DisplayName("Broken JSON returns 400 VALIDATION_ERROR")
  void brokenJsonReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": "))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  @DisplayName("A path parameter of the wrong type returns 400 VALIDATION_ERROR")
  void wrongPathParameterReturns400() throws Exception {
    mockMvc
        .perform(get("/api/applications/abc/status"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details[0]").value("id: грешна стойност"));
  }

  @Test
  @WithMockUser(roles = "APPLICANT")
  @DisplayName("A wrong HTTP method returns 405 METHOD_NOT_ALLOWED")
  void wrongMethodReturns405() throws Exception {
    mockMvc
        .perform(post("/api/applications/1/status").with(csrf()))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.status").value(405))
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
  }

  @Test
  @DisplayName("A wrong Content-Type returns 415 in the common format")
  void wrongContentTypeReturns415() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login").with(csrf()).contentType(MediaType.TEXT_PLAIN).content("hello"))
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.status").value(415))
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }
}
