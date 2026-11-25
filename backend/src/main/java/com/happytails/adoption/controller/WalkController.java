package com.happytails.adoption.controller;

import com.happytails.adoption.dto.BookWalkRequest;
import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.dto.WalkResponse;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.service.WalkBookingService;
import com.happytails.adoption.service.WalkSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/walks")
public class WalkController {

  private final WalkSlotService slotService;
  private final WalkBookingService bookingService;

  public WalkController(WalkSlotService slotService, WalkBookingService bookingService) {
    this.slotService = slotService;
    this.bookingService = bookingService;
  }

  @GetMapping("/slots")
  public FreeSlotsResponse getFreeSlots(@RequestParam(required = false) Long animalId) {
    if (animalId == null) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Трябва да посочите животно.");
    }
    return slotService.findFreeSlots(animalId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WalkResponse bookWalk(@Valid @RequestBody BookWalkRequest request) {
    return bookingService.book(request);
  }
}
