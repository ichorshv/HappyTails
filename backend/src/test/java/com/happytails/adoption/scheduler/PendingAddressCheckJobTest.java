package com.happytails.adoption.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import com.happytails.adoption.exception.GeocodingUnavailableException;
import com.happytails.adoption.repository.ApplicationRepository;
import com.happytails.adoption.service.PendingAddressCheckService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PendingAddressCheckJobTest {

  @Mock private ApplicationRepository applicationRepository;
  @Mock private PendingAddressCheckService checkService;

  private PendingAddressCheckJob job;

  @BeforeEach
  void setUp() {
    job = new PendingAddressCheckJob(applicationRepository, checkService);
    when(applicationRepository.findByStatusAndLatitudeIsNullOrderBySubmittedAtAscIdAsc(
            eq(ApplicationStatus.SUBMITTED), any(Limit.class)))
        .thenReturn(List.of(application(1L), application(2L)));
  }

  @Test
  @DisplayName("Every pending application is checked")
  void checksEveryPendingApplication() {
    job.run();

    verify(checkService).checkAddress(1L);
    verify(checkService).checkAddress(2L);
  }

  @Test
  @DisplayName("The run stops when geocoding is still unavailable")
  void stopsWhenGeocodingIsUnavailable() {
    doThrow(new GeocodingUnavailableException("down", new RuntimeException()))
        .when(checkService)
        .checkAddress(1L);

    job.run();

    verify(checkService).checkAddress(1L);
    verify(checkService, never()).checkAddress(2L);
  }

  @Test
  @DisplayName("A failure of one application does not stop the others")
  void continuesAfterOtherFailure() {
    doThrow(new IllegalStateException("broken")).when(checkService).checkAddress(1L);

    job.run();

    verify(checkService).checkAddress(2L);
  }

  private Application application(Long id) {
    Application application =
        new Application(1L, 7L, "ул. Витоша 10", "София", "Столична", Instant.now());
    ReflectionTestUtils.setField(application, "id", id);
    return application;
  }
}
