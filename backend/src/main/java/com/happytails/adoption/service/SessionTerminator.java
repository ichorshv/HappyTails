package com.happytails.adoption.service;

import com.happytails.adoption.dto.AuthUser;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

/**
 * Ends a user's sessions through the {@link SessionRegistry} (ADR-0003). The session is only marked
 * as expired here; ConcurrentSessionFilter closes it on the next request and answers 401.
 */
@Service
public class SessionTerminator {

  private final SessionRegistry sessionRegistry;

  public SessionTerminator(SessionRegistry sessionRegistry) {
    this.sessionRegistry = sessionRegistry;
  }

  public void endAllSessions(Long userId) {
    sessionRegistry.getAllPrincipals().stream()
        .filter(principal -> principal instanceof AuthUser user && user.id().equals(userId))
        .flatMap(principal -> sessionRegistry.getAllSessions(principal, false).stream())
        .forEach(SessionInformation::expireNow);
  }
}
