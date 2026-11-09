package com.happytails.adoption.geo;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
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
