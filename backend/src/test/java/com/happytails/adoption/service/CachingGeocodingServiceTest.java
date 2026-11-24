package com.happytails.adoption.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.Coordinates;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingGeocodingServiceTest {

  private static final Coordinates SOFIA = new Coordinates(42.6977, 23.3219);

  @Mock private GeocodingService delegate;

  @Test
  @DisplayName("The same address is looked up only once")
  void sameAddressOnce() {
    when(delegate.geocode("Sofia, Vitosha 1")).thenReturn(Optional.of(SOFIA));
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    assertThat(service.geocode("Sofia, Vitosha 1")).contains(SOFIA);
    assertThat(service.geocode("Sofia, Vitosha 1")).contains(SOFIA);

    verify(delegate, times(1)).geocode(anyString());
  }

  @Test
  @DisplayName("Different case and extra spaces count as the same address")
  void sameAddressWrittenDifferently() {
    when(delegate.geocode("Sofia, Vitosha 1")).thenReturn(Optional.of(SOFIA));
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    service.geocode("Sofia, Vitosha 1");
    Optional<Coordinates> second = service.geocode("  SOFIA,   vitosha 1 ");

    assertThat(second).contains(SOFIA);
    verify(delegate, times(1)).geocode(anyString());
  }

  @Test
  @DisplayName("Different addresses are looked up separately")
  void differentAddresses() {
    when(delegate.geocode("A")).thenReturn(Optional.of(SOFIA));
    when(delegate.geocode("B")).thenReturn(Optional.of(SOFIA));
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    service.geocode("A");
    service.geocode("B");

    verify(delegate, times(2)).geocode(anyString());
  }

  @Test
  @DisplayName("An address that was not found is remembered too")
  void notFoundIsCached() {
    when(delegate.geocode("Nowhere")).thenReturn(Optional.empty());
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    assertThat(service.geocode("Nowhere")).isEmpty();
    assertThat(service.geocode("Nowhere")).isEmpty();

    verify(delegate, times(1)).geocode(anyString());
  }

  @Test
  @DisplayName("A failure is not remembered, the next call tries again")
  void failureIsNotCached() {
    when(delegate.geocode("Sofia"))
        .thenThrow(new GeocodingUnavailableException("down", new RuntimeException()))
        .thenReturn(Optional.of(SOFIA));
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    assertThatThrownBy(() -> service.geocode("Sofia"))
        .isInstanceOf(GeocodingUnavailableException.class);
    assertThat(service.geocode("Sofia")).contains(SOFIA);

    verify(delegate, times(2)).geocode("Sofia");
  }

  @Test
  @DisplayName("Blank or missing address is not passed on")
  void blankAddress() {
    CachingGeocodingService service = new CachingGeocodingService(delegate, 10);

    assertThat(service.geocode(null)).isEmpty();
    assertThat(service.geocode("  ")).isEmpty();

    verifyNoInteractions(delegate);
  }

  @Test
  @DisplayName("The least recently used answer is dropped when the cache is full")
  void oldestAnswerIsDropped() {
    when(delegate.geocode(anyString())).thenReturn(Optional.of(SOFIA));
    CachingGeocodingService service = new CachingGeocodingService(delegate, 2);

    service.geocode("a");
    service.geocode("b");
    service.geocode("c");
    service.geocode("a");

    verify(delegate, times(2)).geocode("a");
    verify(delegate, times(1)).geocode("b");
    verify(delegate, times(1)).geocode("c");
  }
}
