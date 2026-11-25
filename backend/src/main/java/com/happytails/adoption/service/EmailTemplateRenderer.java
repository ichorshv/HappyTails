package com.happytails.adoption.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Turns a template name and its parameters into the subject and body of an email. */
@Component
public class EmailTemplateRenderer {

  private static final DateTimeFormatter DATE_TIME =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.of("Europe/Sofia"));

  public EmailMessage render(String template, Map<String, Object> parameters) {
    return switch (template) {
      case "WALK_BOOKED" ->
          new EmailMessage(
              "Разходката ви е запазена",
              paragraph(
                  "Здравейте! Запазихте разходка с "
                      + value(parameters, "animalName")
                      + " на "
                      + value(parameters, "startsAt")
                      + "."));
      case "WALK_CANCELLED" ->
          new EmailMessage(
              "Разходката ви е отменена",
              paragraph(
                  "Здравейте! Разходката с "
                      + value(parameters, "animalName")
                      + " на "
                      + value(parameters, "startsAt")
                      + " е отменена."));
      default -> throw new IllegalArgumentException("Unknown email template: " + template);
    };
  }

  private static String paragraph(String text) {
    return "<p>" + text + "</p><p>HappyTails</p>";
  }

  private static String value(Map<String, Object> parameters, String key) {
    Object value = parameters.get(key);
    if (value == null) {
      return "";
    }
    if (value instanceof Instant instant) {
      return DATE_TIME.format(instant);
    }
    return HtmlUtils.htmlEscape(value.toString());
  }
}
