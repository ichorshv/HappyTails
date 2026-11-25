package com.happytails.adoption.integration;

import com.happytails.adoption.config.NotificationProperties;
import com.happytails.adoption.exception.NotificationDeliveryException;
import com.happytails.adoption.service.EmailMessage;
import com.happytails.adoption.service.EmailTemplateRenderer;
import com.happytails.adoption.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Sends emails over SMTP, for development with Mailpit. Active only with the "dev" profile. */
@Service
@Profile("dev")
public class SmtpNotificationService implements NotificationService {

  private final JavaMailSender mailSender;
  private final NotificationProperties properties;
  private final EmailTemplateRenderer renderer;

  public SmtpNotificationService(
      JavaMailSender mailSender,
      NotificationProperties properties,
      EmailTemplateRenderer renderer) {
    this.mailSender = mailSender;
    this.properties = properties;
    this.renderer = renderer;
  }

  @Override
  public void send(String template, String recipient, Map<String, Object> parameters) {
    EmailMessage message = renderer.render(template, parameters);
    try {
      MimeMessage mime = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
      helper.setFrom(properties.senderEmail(), properties.senderName());
      helper.setTo(recipient);
      helper.setSubject(message.subject());
      helper.setText(message.htmlBody(), true);
      mailSender.send(mime);
    } catch (MessagingException | UnsupportedEncodingException | MailException e) {
      throw new NotificationDeliveryException("The email could not be sent over SMTP.", e);
    }
  }
}
