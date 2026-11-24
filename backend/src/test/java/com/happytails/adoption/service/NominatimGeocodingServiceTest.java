package com.happytails.adoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.happytails.adoption.config.GeocodingProperties;
import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NominatimGeocodingServiceTest {

  private static final String USER_AGENT = "HappyTails/1.0 (test)";
  private static final String FOUND = "[{\"lat\":\"42.6977\",\"lon\":\"23.3219\"}]";

  private MockRestServiceServer server;
  private NominatimGeocodingService service;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    GeocodingProperties properties =
        new GeocodingProperties(
            "nominatim",
            "https://nominatim.test",
            USER_AGENT,
            Duration.ZERO,
            Duration.ofSeconds(5));
    service =
        new NominatimGeocodingService(
            NominatimGeocodingService.createClient(properties, builder),
            new MinIntervalLimiter(Duration.ZERO));
  }

  @Test
  @DisplayName("Returns the coordinates of a found address")
  void returnsCoordinates() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andExpect(queryParam("q", "Sofia"))
        .andExpect(queryParam("format", "jsonv2"))
        .andExpect(queryParam("limit", "1"))
        .andRespond(withSuccess(FOUND, MediaType.APPLICATION_JSON));

    Optional<Coordinates> result = service.geocode("Sofia");

    assertThat(result).contains(new Coordinates(42.6977, 23.3219));
    server.verify();
  }

  @Test
  @DisplayName("Sends the identifying User-Agent header")
  void sendsUserAgent() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andExpect(header("User-Agent", USER_AGENT))
        .andRespond(withSuccess(FOUND, MediaType.APPLICATION_JSON));

    service.geocode("Sofia");

    server.verify();
  }

  @Test
  @DisplayName("Returns empty when Nominatim does not know the address")
  void unknownAddress() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

    assertThat(service.geocode("Nowhere")).isEmpty();
  }

  @Test
  @DisplayName("Blank or missing address makes no request")
  void blankAddressMakesNoRequest() {
    assertThat(service.geocode(null)).isEmpty();
    assertThat(service.geocode("   ")).isEmpty();

    server.verify();
  }

  @Test
  @DisplayName("A server error is reported as GeocodingUnavailableException")
  void serverError() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withServerError());

    assertThatThrownBy(() -> service.geocode("Sofia"))
        .isInstanceOf(GeocodingUnavailableException.class);
  }

  @Test
  @DisplayName("A network failure is reported as GeocodingUnavailableException")
  void networkFailure() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(
            request -> {
              throw new IOException("connection reset");
            });

    assertThatThrownBy(() -> service.geocode("Sofia"))
        .isInstanceOf(GeocodingUnavailableException.class);
  }

  @Test
  @DisplayName("Unreadable coordinates are reported as GeocodingUnavailableException")
  void unreadableCoordinates() {
    server
        .expect(requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(
            withSuccess("[{\"lat\":\"abc\",\"lon\":\"23.3\"}]", MediaType.APPLICATION_JSON));

    assertThatThrownBy(() -> service.geocode("Sofia"))
        .isInstanceOf(GeocodingUnavailableException.class);
  }

  @Test
  @DisplayName("Two lookups are at least one interval apart")
  void respectsMinimumInterval() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer limitedServer = MockRestServiceServer.bindTo(builder).build();
    GeocodingProperties properties =
        new GeocodingProperties(
            "nominatim",
            "https://nominatim.test",
            USER_AGENT,
            Duration.ofMillis(300),
            Duration.ofSeconds(5));
    NominatimGeocodingService limited =
        new NominatimGeocodingService(
            NominatimGeocodingService.createClient(properties, builder),
            new MinIntervalLimiter(Duration.ofMillis(300)));
    limitedServer
        .expect(
            org.springframework.test.web.client.ExpectedCount.twice(),
            requestTo(org.hamcrest.Matchers.containsString("/search")))
        .andRespond(withSuccess(FOUND, MediaType.APPLICATION_JSON));

    limited.geocode("Sofia");
    long start = System.nanoTime();
    limited.geocode("Plovdiv");
    long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

    assertThat(elapsedMillis).isGreaterThanOrEqualTo(250);
    limitedServer.verify();
  }
}
