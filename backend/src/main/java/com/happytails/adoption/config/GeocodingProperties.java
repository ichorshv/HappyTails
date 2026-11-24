package com.happytails.adoption.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings of the geocoding service. The values and their defaults are in application.properties.
 * The provider is "fake" by default, so tests and local runs never call the internet; set
 * GEOCODING_PROVIDER=nominatim to use the real service.
 *
 * @param provider "fake" or "nominatim"
 * @param baseUrl address of the Nominatim server
 * @param userAgent identifies our application, as the Nominatim usage policy requires
 * @param minInterval minimum time between two requests (the policy allows one per second)
 * @param timeout how long to wait for an answer before giving up
 */
@ConfigurationProperties(prefix = "geocoding")
public record GeocodingProperties(
    String provider, String baseUrl, String userAgent, Duration minInterval, Duration timeout) {}
