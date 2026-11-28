package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Animal;
import com.happytails.adoption.enums.AnimalSize;
import com.happytails.adoption.enums.AnimalStatus;
import com.happytails.adoption.enums.Species;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  @Query(
      """
      SELECT a FROM Animal a
      WHERE a.status = :status
        AND (:species IS NULL OR a.species = :species)
        AND (:size IS NULL OR a.size = :size)
        AND a.ageMonths BETWEEN :minAgeMonths AND :maxAgeMonths
      """)
  Page<Animal> findCatalog(
      @Param("status") AnimalStatus status,
      @Param("species") Species species,
      @Param("size") AnimalSize size,
      @Param("minAgeMonths") int minAgeMonths,
      @Param("maxAgeMonths") int maxAgeMonths,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Animal a WHERE a.id = :animalId")
  Optional<Animal> findByIdForUpdate(@Param("animalId") Long animalId);
}
