package com.happytails.adoption.integration;

import com.happytails.adoption.config.NotificationProperties;
import com.happytails.adoption.exception.NotificationDeliveryException;
import com.happytails.adoption.service.EmailMessage;
import com.happytails.adoption.service.EmailTemplateRenderer;
import com.happytails.adoption.service.NotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Sends real emails through the Brevo API. Active only with the "brevo" profile. */
@Service
@Profile("brevo")
public class BrevoNotificationService implements NotificationService {

  private static final String SEND_EMAIL_URL = "https://api.brevo.com/v3/smtp/email";

  private final RestClient restClient;
  private final NotificationProperties properties;
  private final EmailTemplateRenderer renderer;

  public BrevoNotificationService(
      RestClient.Builder restClientBuilder,
      NotificationProperties properties,
      EmailTemplateRenderer renderer) {
    if (properties.brevoApiKey() == null || properties.brevoApiKey().isBlank()) {
      throw new IllegalStateException("BREVO_API_KEY is not set.");
    }
    this.restClient = restClientBuilder.build();
    this.properties = properties;
    this.renderer = renderer;
  }

  @Override
  public void send(String template, String recipient, Map<String, Object> parameters) {
    EmailMessage message = renderer.render(template, parameters);
    Map<String, Object> body =
        Map.of(
            "sender",
            Map.of("name", properties.senderName(), "email", properties.senderEmail()),
            "to",
            List.of(Map.of("email", recipient)),
            "subject",
            message.subject(),
            "htmlContent",
            message.htmlBody());
    try {
      restClient
          .post()
          .uri(SEND_EMAIL_URL)
          .header("api-key", properties.brevoApiKey())
          .contentType(MediaType.APPLICATION_JSON)
          .body(body)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      throw new NotificationDeliveryException("Brevo did not accept the email.", e);
    }
  }
}
