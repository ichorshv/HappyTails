package com.happytails.adoption.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.service.FakeGeocodingService;
import org.junit.jupiter.api.Test;

public class FakeGeocodingServiceTest {

  private final FakeGeocodingService service = new FakeGeocodingService();

  @Test
  void returnsFixedCoordinatesForAnyAddress() {
    Optional<Coordinates> result = service.geocode("ул. Витоша 1, София");

    assertTrue(result.isPresent());
    assertEquals(new Coordinates(42.6977, 23.3219), result.get());
  }

  @Test
  void returnsEmptyForBlankAddress() {
    Optional<Coordinates> result = service.geocode("");

    assertTrue(result.isEmpty());
  }
}
