package com.happytails.adoption.exception;

/** The email could not be delivered. A later retry (outbox) can catch this. */
public class NotificationDeliveryException extends RuntimeException {

  public NotificationDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
