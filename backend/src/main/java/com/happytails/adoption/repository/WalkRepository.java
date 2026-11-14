package com.happytails.adoption.repository;

import com.happytails.adoption.entity.Walk;
import com.happytails.adoption.enums.WalkStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkRepository extends JpaRepository<Walk, Long> {

  List<Walk> findByApplicantIdAndStatusOrderByStartsAt(Long applicantId, WalkStatus status);

  List<Walk> findByStartsAtGreaterThanEqualAndStartsAtLessThanAndStatusOrderByStartsAt(
      Instant from, Instant to, WalkStatus status);
}
