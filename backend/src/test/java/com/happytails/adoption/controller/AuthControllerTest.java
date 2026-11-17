package com.happytails.adoption.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private ResultActions register(String email, String password, String fullName) throws Exception {
    String body =
        """
        {"email": "%s", "password": "%s", "fullName": "%s"}
        """
            .formatted(email, password, fullName);
    return mockMvc.perform(
        post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));
  }

  @Test
  @DisplayName("Registration creates an applicant with a BCrypt password")
  void registerCreatesApplicant() throws Exception {
    register("Maria@Example.com", "Parola123", "  Мария Иванова ")
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.email").value("maria@example.com"))
        .andExpect(jsonPath("$.fullName").value("Мария Иванова"))
        .andExpect(jsonPath("$.role").value("APPLICANT"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());

    User user = userRepository.findByEmail("maria@example.com").orElseThrow();
    assertEquals(Role.APPLICANT, user.getRole());
    assertTrue(user.isActive());
    assertNotEquals("Parola123", user.getPasswordHash());
    assertTrue(passwordEncoder.matches("Parola123", user.getPasswordHash()));
  }

  @Test
  @DisplayName("The same email in other letter case cannot register twice")
  void duplicateEmailIsRejected() throws Exception {
    register("maria@example.com", "Parola123", "Мария Иванова").andExpect(status().isCreated());

    register("MARIA@example.com", "Parola456", "Друга Мария")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.code").value("AUTH_EMAIL_TAKEN"));
  }

  @Test
  @DisplayName("Invalid fields return VALIDATION_ERROR with details")
  void invalidFieldsAreRejected() throws Exception {
    register("not-an-email", "parola", "М")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details", hasItem(startsWith("email:"))))
        .andExpect(jsonPath("$.details", hasItem(startsWith("password:"))))
        .andExpect(jsonPath("$.details", hasItem(startsWith("fullName:"))));
  }

  @Test
  @DisplayName("A password without a digit is rejected")
  void passwordNeedsLetterAndDigit() throws Exception {
    register("ivan@example.com", "SamoBukvi", "Иван Петров")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  @DisplayName("A password longer than 72 bytes is rejected")
  void passwordOver72BytesIsRejected() throws Exception {
    // 40 Cyrillic letters + 1 digit = 41 characters, but 81 bytes.
    register("ivan@example.com", "ж".repeat(40) + "1", "Иван Петров")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}
