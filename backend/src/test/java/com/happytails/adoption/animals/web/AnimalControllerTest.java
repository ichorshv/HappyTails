package com.happytails.adoption.animals.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
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
}
