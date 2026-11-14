package com.happytails.adoption.service;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingNotificationService implements NotificationService {

  private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

  @Override
  public void send(String template, String recipient, Map<String, Object> parameters) {
    log.info("NOTIFICATION template={} to={} params={}", template, recipient, parameters);
  }
}
