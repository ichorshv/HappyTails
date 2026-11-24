package com.happytails.adoption.service;

import com.happytails.adoption.dto.Coordinates;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Geocoding stand-in that never calls the internet. Active when {@code geocoding.provider} is
 * "fake" (the default), so tests and local runs do not depend on Nominatim.
 */
@Service
@ConditionalOnProperty(name = "geocoding.provider", havingValue = "fake", matchIfMissing = true)
public class FakeGeocodingService implements GeocodingService {

  // Sofia city centre - returned for every non-blank address
  private static final Coordinates FIXED_LOCATION = new Coordinates(42.6977, 23.3219);

  @Override
  public Optional<Coordinates> geocode(String address) {
    if (address == null || address.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(FIXED_LOCATION);
  }
}
