package com.happytails.adoption.service;

import com.happytails.adoption.dto.Coordinates;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Remembers the answers of another geocoding service, so the same address is looked up only once.
 *
 * <p>Both "found" and "not found" answers are remembered. A failure (service unavailable) is not,
 * so the next call tries again. At most {@code maxEntries} answers are kept; the least recently
 * used one is dropped first.
 */
public class CachingGeocodingService implements GeocodingService {

  private final GeocodingService delegate;
  private final Map<String, Optional<Coordinates>> cache;

  /**
   * @param delegate the service that really looks addresses up
   * @param maxEntries how many answers to keep
   */
  public CachingGeocodingService(GeocodingService delegate, int maxEntries) {
    this.delegate = delegate;
    this.cache =
        Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
              @Override
              protected boolean removeEldestEntry(Map.Entry<String, Optional<Coordinates>> eldest) {
                return size() > maxEntries;
              }
            });
  }

  @Override
  public Optional<Coordinates> geocode(String address) {
    if (address == null || address.isBlank()) {
      return Optional.empty();
    }
    String key = normalize(address);
    Optional<Coordinates> cached = cache.get(key);
    if (cached != null) {
      return cached;
    }
    Optional<Coordinates> found = delegate.geocode(address);
    cache.put(key, found);
    return found;
  }

  // Same address written with different case or extra spaces must give the same key.
  private static String normalize(String address) {
    return address.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
  }
}
