package com.happytails.adoption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTemplateRendererTest {

  private final EmailTemplateRenderer renderer = new EmailTemplateRenderer();

  @Test
  @DisplayName("WALK_BOOKED shows the animal and the time in Sofia time")
  void walkBooked() {
    EmailMessage message =
        renderer.render(
            "WALK_BOOKED",
            Map.of("animalName", "Рекс", "startsAt", Instant.parse("2026-11-17T07:00:00Z")));

    assertEquals("Разходката ви е запазена", message.subject());
    assertTrue(message.htmlBody().contains("Рекс"));
    assertTrue(message.htmlBody().contains("17.11.2026 09:00"));
  }

  @Test
  @DisplayName("WALK_CANCELLED has its own subject")
  void walkCancelled() {
    EmailMessage message =
        renderer.render(
            "WALK_CANCELLED",
            Map.of("animalName", "Рекс", "startsAt", Instant.parse("2026-11-17T07:00:00Z")));

    assertEquals("Разходката ви е отменена", message.subject());
  }

  @Test
  @DisplayName("HTML in parameters is escaped")
  void escapesHtml() {
    EmailMessage message =
        renderer.render(
            "WALK_BOOKED",
            Map.of("animalName", "<b>Рекс</b>", "startsAt", Instant.parse("2026-11-17T07:00:00Z")));

    assertFalse(message.htmlBody().contains("<b>"));
    assertTrue(message.htmlBody().contains("&lt;b&gt;"));
  }

  @Test
  @DisplayName("An unknown template is rejected")
  void unknownTemplate() {
    assertThrows(IllegalArgumentException.class, () -> renderer.render("NOPE", Map.of()));
  }
}
