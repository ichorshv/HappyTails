package com.happytails.adoption.repository;

import com.happytails.adoption.entity.ApplicationAnswers;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationAnswersRepository extends JpaRepository<ApplicationAnswers, Long> {

  Optional<ApplicationAnswers> findByApplicationId(Long applicationId);
}
