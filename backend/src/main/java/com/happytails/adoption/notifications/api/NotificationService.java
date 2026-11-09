package com.happytails.adoption.notifications.api;

import java.util.Map;

/**
 * Public contract of the notifications module.
 * Other modules call this interface to send emails and never depend on a concrete provider.
 */
public interface NotificationService {

    /**
     * Sends a notification to a recipient.
     *
     * @param template   name of the message template, e.g. "YOUR_TURN"
     * @param recipient  recipient email address
     * @param parameters values substituted in the template
     */
    void send(String template, String recipient, Map<String, Object> parameters);
}