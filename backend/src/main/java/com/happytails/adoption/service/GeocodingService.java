package com.happytails.adoption.service;

import com.happytails.adoption.dto.Coordinates;

import java.util.Optional;

public interface GeocodingService {

  Optional<Coordinates> geocode(String address);
}
