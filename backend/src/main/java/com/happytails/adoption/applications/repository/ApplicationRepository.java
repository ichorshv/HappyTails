package com.happytails.adoption.applications.repository;

import com.happytails.adoption.applications.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {}