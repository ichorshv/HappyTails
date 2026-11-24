package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AnimalStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  Page<Animal> findByStatus(AnimalStatus status, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Animal a WHERE a.id = :animalId")
  Optional<Animal> findByIdForUpdate(@Param("animalId") Long animalId);
}
