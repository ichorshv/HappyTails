package com.happytails.adoption.controller;

import com.happytails.adoption.dto.FreeSlotsResponse;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.service.WalkSlotService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/walks")
public class WalkController {

  private final WalkSlotService slotService;

  public WalkController(WalkSlotService slotService) {
    this.slotService = slotService;
  }

  @GetMapping("/slots")
  public FreeSlotsResponse getFreeSlots(@RequestParam(required = false) Long animalId) {
    if (animalId == null) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Трябва да посочите животно.");
    }
    return slotService.findFreeSlots(animalId);
  }
}
