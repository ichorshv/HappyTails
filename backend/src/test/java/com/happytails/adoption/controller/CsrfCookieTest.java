package com.happytails.adoption.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Checks the real CSRF cookie flow the frontend uses. It needs a fresh context: {@code csrf()} from
 * Spring Security Test replaces the token repository for the whole context, so this class must not
 * use it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CsrfCookieTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The XSRF-TOKEN cookie from /me works as the X-XSRF-TOKEN header")
  void csrfCookieFromMeWorksForRegister() throws Exception {
    MvcResult me =
        mockMvc
            .perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
            .andReturn();
    String setCookie = me.getResponse().getHeader("Set-Cookie");
    assertNotNull(setCookie);
    assertTrue(setCookie.startsWith("XSRF-TOKEN="));
    String token = setCookie.substring("XSRF-TOKEN=".length(), setCookie.indexOf(';'));

    String body =
        """
        {"email": "csrf@example.com", "password": "Parola123", "fullName": "Тест Тестов",
         "city": "София", "municipality": "Столична"}
        """;
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .cookie(new Cookie("XSRF-TOKEN", token))
                .header("X-XSRF-TOKEN", token))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("A POST with a wrong CSRF header is refused")
  void wrongCsrfHeaderIsRefused() throws Exception {
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"a@b.bg\", \"password\": \"Parola123\"}")
                .cookie(new Cookie("XSRF-TOKEN", "real-token"))
                .header("X-XSRF-TOKEN", "other-token"))
        .andExpect(status().isForbidden());
  }
}
