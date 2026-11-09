package com.happytails.adoption.walks.repository;

import com.happytails.adoption.walks.domain.WalkSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkSettingsRepository extends JpaRepository<WalkSettings, Long> {}
