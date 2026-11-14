package com.happytails.adoption.entity;

import com.happytails.adoption.enums.WalkStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "walk")
public class Walk {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "application_id", nullable = false)
  private Long applicationId;

  @Column(name = "animal_id", nullable = false)
  private Long animalId;

  @Column(name = "applicant_id", nullable = false)
  private Long applicantId;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at", nullable = false)
  private Instant endsAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private WalkStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Walk() {}

  public Walk(
      Long applicationId,
      Long animalId,
      Long applicantId,
      Instant startsAt,
      Instant endsAt,
      Instant createdAt) {
    this.applicationId = applicationId;
    this.animalId = animalId;
    this.applicantId = applicantId;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.createdAt = createdAt;
    this.status = WalkStatus.BOOKED;
  }

  public Long getId() {
    return id;
  }

  public Long getApplicationId() {
    return applicationId;
  }

  public Long getAnimalId() {
    return animalId;
  }

  public Long getApplicantId() {
    return applicantId;
  }

  public Instant getStartsAt() {
    return startsAt;
  }

  public Instant getEndsAt() {
    return endsAt;
  }

  public WalkStatus getStatus() {
    return status;
  }

  public void setStatus(WalkStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
