package com.happytails.adoption.repository;

import com.happytails.adoption.entity.WalkSettings;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface WalkSettingsRepository extends JpaRepository<WalkSettings, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from WalkSettings s where s.id = 1")
  Optional<WalkSettings> findForUpdate();
}
