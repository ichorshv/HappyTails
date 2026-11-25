package com.happytails.adoption.service;

/** A ready-to-send email: subject and HTML body. */
public record EmailMessage(String subject, String htmlBody) {}
