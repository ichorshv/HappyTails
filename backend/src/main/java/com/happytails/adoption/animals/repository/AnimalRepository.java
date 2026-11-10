package com.happytails.adoption.animals.repository;

import com.happytails.adoption.animals.domain.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {}
