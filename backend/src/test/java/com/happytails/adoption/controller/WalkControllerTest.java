package com.happytails.adoption.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.service.WalkBookingService;
import com.happytails.adoption.service.WalkSlotService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class WalkControllerTest {

  private final WalkSlotService slotService = mock(WalkSlotService.class);
  private final WalkBookingService bookingService = mock(WalkBookingService.class);
  private final WalkController controller = new WalkController(slotService, bookingService);

  @Test
  @DisplayName("A missing animalId returns 400 with code VALIDATION_ERROR")
  void missingAnimalIdIsRejected() {
    ApiException error = assertThrows(ApiException.class, () -> controller.getFreeSlots(null));

    assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    assertEquals("VALIDATION_ERROR", error.getCode());
  }

  @Test
  @DisplayName("The controller returns what the service returns")
  void responseComesFromService() {
    FreeSlotsResponse expected = new FreeSlotsResponse(12L, 60, List.of());
    when(slotService.findFreeSlots(12L)).thenReturn(expected);

    assertSame(expected, controller.getFreeSlots(12L));
  }
}
