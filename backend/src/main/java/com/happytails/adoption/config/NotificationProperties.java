package com.happytails.adoption.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Email settings, see application.properties. The Brevo key comes only from the environment. */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(String senderName, String senderEmail, String brevoApiKey) {}
