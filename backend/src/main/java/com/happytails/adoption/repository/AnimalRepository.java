package com.happytails.adoption.animals.repository;

import com.happytails.adoption.animals.api.AnimalStatus;
import com.happytails.adoption.animals.domain.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  Page<Animal> findByStatus(AnimalStatus status, Pageable pageable);
}
