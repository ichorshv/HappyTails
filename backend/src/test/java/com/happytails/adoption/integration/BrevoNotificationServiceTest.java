package com.happytails.adoption.integration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.happytails.adoption.config.NotificationProperties;
import com.happytails.adoption.exception.NotificationDeliveryException;
import com.happytails.adoption.service.EmailTemplateRenderer;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class BrevoNotificationServiceTest {

  private static final String URL = "https://api.brevo.com/v3/smtp/email";
  private static final Map<String, Object> PARAMS =
      Map.of("animalName", "Рекс", "startsAt", Instant.parse("2026-11-17T07:00:00Z"));

  private final RestClient.Builder builder = RestClient.builder();
  private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
  private final NotificationProperties properties =
      new NotificationProperties("HappyTails", "sender@example.com", "test-key");
  private final BrevoNotificationService service =
      new BrevoNotificationService(builder, properties, new EmailTemplateRenderer());

  @Test
  @DisplayName("The email goes to Brevo with the key in the api-key header")
  void sendsEmailToBrevo() {
    server
        .expect(requestTo(URL))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("api-key", "test-key"))
        .andExpect(jsonPath("$.sender.email").value("sender@example.com"))
        .andExpect(jsonPath("$.to[0].email").value("anna@example.com"))
        .andExpect(jsonPath("$.subject").value("Разходката ви е запазена"))
        .andRespond(withSuccess("{\"messageId\":\"1\"}", MediaType.APPLICATION_JSON));

    service.send("WALK_BOOKED", "anna@example.com", PARAMS);

    server.verify();
  }

  @Test
  @DisplayName("When Brevo answers with an error the call fails with NotificationDeliveryException")
  void brevoRejectsEmail() {
    server.expect(requestTo(URL)).andRespond(withServerError());

    assertThrows(
        NotificationDeliveryException.class,
        () -> service.send("WALK_BOOKED", "anna@example.com", PARAMS));
  }

  @Test
  @DisplayName("A missing API key stops the application at startup")
  void missingKeyFailsFast() {
    NotificationProperties noKey = new NotificationProperties("HappyTails", "a@b.bg", "");

    assertThrows(
        IllegalStateException.class,
        () ->
            new BrevoNotificationService(RestClient.builder(), noKey, new EmailTemplateRenderer()));
  }
}
