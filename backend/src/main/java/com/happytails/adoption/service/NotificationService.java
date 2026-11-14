package com.happytails.adoption.service;

import java.util.Map;

public interface NotificationService {

  void send(String template, String recipient, Map<String, Object> parameters);
}
