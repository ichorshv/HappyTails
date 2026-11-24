package com.happytails.adoption.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.happytails.adoption.config.GeocodingProperties;
import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Finds coordinates of an address with the public Nominatim (OpenStreetMap) service.
 *
 * <p>Follows the Nominatim usage policy: at most one request per second (see {@link
 * MinIntervalLimiter}) and an identifying User-Agent header (see {@link #createClient}).
 */
public class NominatimGeocodingService implements GeocodingService {

  private final RestClient restClient;
  private final MinIntervalLimiter limiter;

  public NominatimGeocodingService(RestClient restClient, MinIntervalLimiter limiter) {
    this.restClient = restClient;
    this.limiter = limiter;
  }

  /**
   * Prepares the HTTP client: base address of the server and the User-Agent header that the
   * Nominatim policy requires.
   *
   * @param properties geocoding settings
   * @param builder client builder (the caller may already have set timeouts on it)
   * @return the client used for all requests to Nominatim
   */
  public static RestClient createClient(
      GeocodingProperties properties, RestClient.Builder builder) {
    return builder
        .baseUrl(properties.baseUrl())
        .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
        .build();
  }

  /**
   * Looks the address up.
   *
   * @param address free-text address
   * @return the coordinates, or empty when the address is blank or Nominatim does not know it
   * @throws GeocodingUnavailableException when Nominatim cannot be reached or answers badly
   */
  @Override
  public Optional<Coordinates> geocode(String address) {
    if (address == null || address.isBlank()) {
      return Optional.empty();
    }
    limiter.acquire();

    Place[] places;
    try {
      places =
          restClient
              .get()
              .uri("/search?q={address}&format=jsonv2&limit=1", address)
              .retrieve()
              .body(Place[].class);
    } catch (RestClientException e) {
      throw new GeocodingUnavailableException("Nominatim request failed", e);
    }

    if (places == null || places.length == 0) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          new Coordinates(
              Double.parseDouble(places[0].lat()), Double.parseDouble(places[0].lon())));
    } catch (NumberFormatException | NullPointerException e) {
      throw new GeocodingUnavailableException("Nominatim returned unreadable coordinates", e);
    }
  }

  /** One found place; Nominatim sends the coordinates as text. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  record Place(String lat, String lon) {}
}
