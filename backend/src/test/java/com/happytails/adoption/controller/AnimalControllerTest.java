package com.happytails.adoption.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnimalControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void catalogIsPublicAndShowsOnlyAvailableAnimals() throws Exception {
    mockMvc
        .perform(get("/api/animals"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.content[*].id", hasItems(1, 6, 8)))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(7))))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(9))))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(10))));
  }

  @Test
  void catalogReturnsSummaryDtoWithoutEntityDetails() throws Exception {
    mockMvc
        .perform(get("/api/animals"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").exists())
        .andExpect(jsonPath("$.content[0].species").exists())
        .andExpect(jsonPath("$.content[0].description").doesNotExist())
        .andExpect(jsonPath("$.content[0].status").doesNotExist())
        .andExpect(jsonPath("$.content[0].requirements").doesNotExist());
  }

  @Test
  void filtersBySpecies() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("species", "CAT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItem(8)))
        .andExpect(jsonPath("$.content[*].species", everyItem(is("CAT"))));
  }

  @Test
  void filtersBySize() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("size", "LARGE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItems(1, 2)))
        .andExpect(jsonPath("$.content[*].size", everyItem(is("LARGE"))));
  }

  @Test
  void filtersByAgeGroup() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("age", "PUPPY"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItems(5, 6)))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(7))))
        .andExpect(jsonPath("$.content[*].ageMonths", everyItem(lessThanOrEqualTo(11))));
  }

  @Test
  void combinesFilters() throws Exception {
    mockMvc
        .perform(
            get("/api/animals")
                .param("species", "DOG")
                .param("size", "SMALL")
                .param("age", "ADULT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].id", hasItem(3)))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(6))))
        .andExpect(jsonPath("$.content[*].id", not(hasItem(8))));
  }

  @Test
  void returnsEmptyPageAfterTheLastOne() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("page", "50"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(50))
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void rejectsUnknownFilterValue() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("species", "BIRD"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void rejectsNegativePage() throws Exception {
    mockMvc
        .perform(get("/api/animals").param("page", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}
