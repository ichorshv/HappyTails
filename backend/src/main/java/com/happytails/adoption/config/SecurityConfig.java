package com.happytails.adoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/** Login with a server session and CSRF cookie, see docs/adr/0003-session-vs-jwt.md. */
@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(
      HttpSecurity http,
      SecurityContextRepository securityContextRepository,
      CsrfTokenRepository csrfTokenRepository)
      throws Exception {
    http.csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
        .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
        .securityContext(context -> context.securityContextRepository(securityContextRepository))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            errors ->
                errors
                    .authenticationEntryPoint(JsonErrorHandlers.unauthorized())
                    .accessDeniedHandler(JsonErrorHandlers.forbidden()))
        // Roles come from the contracts in docs/api. The first matching rule wins, so the more
        // specific addresses come first. Anything not listed is closed.
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/error")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/animals/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/admin/users/**",
                        "/api/admin/walk-settings/**",
                        "/api/admin/geo-settings/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/admin/**", "/api/staff/**")
                    .hasAnyRole("STAFF", "ADMIN")
                    // Owner, staff and admin; the owner check is in the service.
                    .requestMatchers(HttpMethod.GET, "/api/applications/*/status")
                    .authenticated()
                    .requestMatchers("/api/applications/**", "/api/walks/**")
                    .hasRole("APPLICANT")
                    .anyRequest()
                    .denyAll());
    return http.build();
  }

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  /** The XSRF-TOKEN cookie is readable by JavaScript, so Axios can send it back as a header. */
  @Bean
  CsrfTokenRepository csrfTokenRepository() {
    return CookieCsrfTokenRepository.withHttpOnlyFalse();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
