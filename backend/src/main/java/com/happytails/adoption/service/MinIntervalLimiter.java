package com.happytails.adoption.service;

import com.happytails.adoption.exception.GeocodingUnavailableException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Lets callers through at most once per the given interval. Nominatim's usage policy allows one
 * request per second, so every request to it first calls {@link #acquire()}, which waits if the
 * previous request was too recent.
 */
public class MinIntervalLimiter {

  private final long intervalNanos;
  private long nextAllowedAtNanos;

  /**
   * @param minInterval minimum time between two permitted calls
   */
  public MinIntervalLimiter(Duration minInterval) {
    this.intervalNanos = minInterval.toNanos();
    this.nextAllowedAtNanos = System.nanoTime();
  }

  /**
   * Blocks until the next call is allowed. Callers are served one at a time, so the minimum
   * interval holds even when several threads ask together.
   *
   * @throws GeocodingUnavailableException if the waiting thread is interrupted
   */
  public synchronized void acquire() {
    long waitNanos = nextAllowedAtNanos - System.nanoTime();
    if (waitNanos > 0) {
      try {
        TimeUnit.NANOSECONDS.sleep(waitNanos);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new GeocodingUnavailableException("Interrupted while waiting to call geocoding", e);
      }
    }
    nextAllowedAtNanos = System.nanoTime() + intervalNanos;
  }
}
