package com.happytails.adoption.walks.repository;

import com.happytails.adoption.walks.domain.Walk;
import com.happytails.adoption.walks.domain.WalkStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkRepository extends JpaRepository<Walk, Long> {

  List<Walk> findByApplicantIdAndStatusOrderByStartsAt(Long applicantId, WalkStatus status);

  List<Walk> findByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusOrderByStartsAt(
      Instant from, Instant to, WalkStatus status);
}
