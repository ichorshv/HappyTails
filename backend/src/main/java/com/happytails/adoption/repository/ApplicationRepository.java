package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {}
