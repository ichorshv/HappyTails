package com.happytails.adoption.animals.service;

import com.happytails.adoption.animals.api.AnimalStatus;
import com.happytails.adoption.animals.domain.Animal;
import com.happytails.adoption.animals.dto.AnimalSummaryResponse;
import com.happytails.adoption.animals.dto.PageResponse;
import com.happytails.adoption.animals.mapper.AnimalMapper;
import com.happytails.adoption.animals.repository.AnimalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimalCatalogService {

  static final int PAGE_SIZE = 12;

  private static final Sort NEWEST_FIRST =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  private final AnimalRepository animalRepository;
  private final AnimalMapper animalMapper;

  public AnimalCatalogService(AnimalRepository animalRepository, AnimalMapper animalMapper) {
    this.animalRepository = animalRepository;
    this.animalMapper = animalMapper;
  }

  @Transactional(readOnly = true)
  public PageResponse<AnimalSummaryResponse> findAvailable() {
    Page<Animal> animals =
        animalRepository.findByStatus(
            AnimalStatus.AVAILABLE, PageRequest.of(0, PAGE_SIZE, NEWEST_FIRST));

    return new PageResponse<>(
        animals.getContent().stream().map(animalMapper::toSummary).toList(),
        animals.getNumber(),
        animals.getTotalElements(),
        animals.getTotalPages());
  }
}
