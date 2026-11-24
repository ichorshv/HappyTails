package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Application;
import com.happytails.adoption.enums.ApplicationStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

  List<Application> findByAnimalIdAndStatusInOrderBySubmittedAtAscIdAsc(
      Long animalId, Collection<ApplicationStatus> statuses);

  List<Application> findByAnimalIdOrderBySubmittedAtAscIdAsc(Long animalId);

  boolean existsByApplicantIdAndAnimalIdAndStatusIn(
      Long applicantId, Long animalId, Collection<ApplicationStatus> statuses);

  long countByApplicantIdAndStatusIn(Long applicantId, Collection<ApplicationStatus> statuses);

  List<Application> findByStatusAndLatitudeIsNullOrderBySubmittedAtAscIdAsc(
      ApplicationStatus status, Limit limit);
}
