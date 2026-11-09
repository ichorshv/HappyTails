package com.happytails.adoption.notifications.api;

import java.util.Map;

public interface NotificationService {

  void send(String template, String recipient, Map<String, Object> parameters);
}
