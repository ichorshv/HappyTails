package com.happytails.adoption.config;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

/** 401 and 403 from Spring Security in the common error format. */
final class JsonErrorHandlers {

  private JsonErrorHandlers() {}

  static AuthenticationEntryPoint unauthorized() {
    return (request, response, ex) ->
        write(response, 401, "UNAUTHORIZED", "Не сте влезли в профила си.");
  }

  static AccessDeniedHandler forbidden() {
    return (request, response, ex) ->
        write(response, 403, "FORBIDDEN", "Нямате достъп до тази страница.");
  }

  /** The admin deactivated the user, so the session was ended. */
  static SessionInformationExpiredStrategy sessionExpired() {
    return event -> write(event.getResponse(), 401, "UNAUTHORIZED", "Не сте влезли в профила си.");
  }

  private static void write(HttpServletResponse response, int status, String code, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response
        .getWriter()
        .write(
            "{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"details\":[]}"
                .formatted(status, code, message));
  }
}
