package com.happytails.adoption.animals.web;

import com.happytails.adoption.animals.dto.AnimalSummaryResponse;
import com.happytails.adoption.animals.dto.PageResponse;
import com.happytails.adoption.animals.service.AnimalCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

  private final AnimalCatalogService catalogService;

  public AnimalController(AnimalCatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping
  public PageResponse<AnimalSummaryResponse> getCatalog() {
    return catalogService.findAvailable();
  }
}
