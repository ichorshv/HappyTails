package com.happytails.adoption.controller;

import com.happytails.adoption.dto.AnimalSummaryResponse;
import com.happytails.adoption.dto.PageResponse;
import com.happytails.adoption.enums.AgeGroup;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.Species;
import com.happytails.adoption.exception.ApiException;
import com.happytails.adoption.service.AnimalCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

  private final AnimalCatalogService catalogService;

  public AnimalController(AnimalCatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping
  public PageResponse<AnimalSummaryResponse> getCatalog(
      @RequestParam(required = false) Species species,
      @RequestParam(required = false) AgeGroup age,
      @RequestParam(required = false) AnimalSize size,
      @RequestParam(defaultValue = "0") int page) {
    if (page < 0) {
      throw new ApiException(
          HttpStatus.BAD_REQUEST,
          "VALIDATION_ERROR",
          "Номерът на страницата не може да е отрицателен.");
    }
    return catalogService.findAvailable(species, age, size, page);
  }
}
