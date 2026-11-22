package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AnimalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  Page<Animal> findByStatus(AnimalStatus status, Pageable pageable);
}
