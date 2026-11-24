package com.happytails.adoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.happytails.adoption.exception.GeocodingUnavailableException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MinIntervalLimiterTest {

  private static final Duration INTERVAL = Duration.ofMillis(300);

  private static long elapsedMillis(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }

  @Test
  @DisplayName("The first call is not delayed")
  void firstCallDoesNotWait() {
    MinIntervalLimiter limiter = new MinIntervalLimiter(INTERVAL);

    long start = System.nanoTime();
    limiter.acquire();

    assertThat(elapsedMillis(start)).isLessThan(150);
  }

  @Test
  @DisplayName("The second call waits for the minimum interval")
  void secondCallWaits() {
    MinIntervalLimiter limiter = new MinIntervalLimiter(INTERVAL);
    limiter.acquire();

    long start = System.nanoTime();
    limiter.acquire();

    assertThat(elapsedMillis(start)).isGreaterThanOrEqualTo(250);
  }

  @Test
  @DisplayName("Three calls take at least two intervals")
  void threeCallsTakeTwoIntervals() {
    MinIntervalLimiter limiter = new MinIntervalLimiter(INTERVAL);

    long start = System.nanoTime();
    limiter.acquire();
    limiter.acquire();
    limiter.acquire();

    assertThat(elapsedMillis(start)).isGreaterThanOrEqualTo(550);
  }

  @Test
  @DisplayName("An interrupted wait throws GeocodingUnavailableException and keeps the flag")
  void interruptedWaitThrows() {
    MinIntervalLimiter limiter = new MinIntervalLimiter(Duration.ofSeconds(5));
    limiter.acquire();

    Thread.currentThread().interrupt();

    assertThatThrownBy(limiter::acquire).isInstanceOf(GeocodingUnavailableException.class);
    assertThat(Thread.interrupted()).isTrue();
  }
}
