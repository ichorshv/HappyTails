package com.happytails.adoption.geo;

import java.util.Optional;

public interface GeocodingService {

    Optional<Coordinates> geocode(String address);
}
