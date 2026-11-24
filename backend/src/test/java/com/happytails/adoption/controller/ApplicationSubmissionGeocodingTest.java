package com.happytails.adoption.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.entity.Application;
import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.enums.Role;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.repository.UserRepository;
import com.happytails.adoption.service.GeocodingService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * How POST /api/applications reacts to the three answers of the geocoding service: address found,
 * address not found, service unavailable. The geocoding service is replaced by a mock.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApplicationSubmissionGeocodingTest {

  private static final String EMAIL = "applicant@example.com";
  private static final long ANIMAL_ID = 1L;

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ApplicationRepository applicationRepository;

  @MockitoBean private GeocodingService geocodingService;

  private void createUser() {
    userRepository.save(
        new User(
            EMAIL,
            "test-password-hash",
            "Test User",
            Role.APPLICANT,
            Instant.parse("2026-11-01T10:00:00Z")));
  }

  private ResultActions submit() throws Exception {
    return mockMvc.perform(
        post("/api/applications")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                """
                                {
                                  "animalId": 1,
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
                                """));
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("A found address is saved with its coordinates and the application is queued (201)")
  void foundAddressIsSavedWithCoordinates() throws Exception {
    createUser();
    when(geocodingService.geocode(anyString()))
        .thenReturn(Optional.of(new Coordinates(42.6977, 23.3219)));

    submit().andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("YOUR_TURN"));

    List<Application> saved =
        applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(ANIMAL_ID);
    assertThat(saved).hasSize(1);
    assertThat(saved.get(0).getLatitude()).isEqualTo(42.6977);
    assertThat(saved.get(0).getLongitude()).isEqualTo(23.3219);
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("An address that is not found is rejected with 422 ADDRESS_NOT_FOUND")
  void unknownAddressIsRejected() throws Exception {
    createUser();
    when(geocodingService.geocode(anyString())).thenReturn(Optional.empty());

    submit()
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));

    assertThat(applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(ANIMAL_ID)).isEmpty();
  }

  @Test
  @WithMockUser(username = EMAIL, roles = "APPLICANT")
  @DisplayName("When geocoding is unavailable the application is kept as SUBMITTED (202)")
  void unavailableGeocodingKeepsApplicationSubmitted() throws Exception {
    createUser();
    when(geocodingService.geocode(anyString()))
        .thenThrow(new GeocodingUnavailableException("down", new RuntimeException()));

    submit().andExpect(status().isAccepted()).andExpect(jsonPath("$.status").value("SUBMITTED"));

    List<Application> saved =
        applicationRepository.findByAnimalIdOrderBySubmittedAtAscIdAsc(ANIMAL_ID);
    assertThat(saved).hasSize(1);
    assertThat(saved.get(0).getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
    assertThat(saved.get(0).getLatitude()).isNull();
  }
}
