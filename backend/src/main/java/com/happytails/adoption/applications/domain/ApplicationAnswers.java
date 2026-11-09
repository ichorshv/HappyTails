package com.happytails.adoption.applications.domain;

import com.happytails.adoption.applications.dto.HousingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "application_answers")
public class ApplicationAnswers {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "application_id", nullable = false, unique = true)
  private Application application;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private HousingType housingType;

  @Column(nullable = false)
  private boolean hasYard;

  @Column(nullable = false)
  private boolean hasChildrenUnder6;

  @Column(nullable = false)
  private boolean hasCats;

  @Column(nullable = false)
  private boolean hasDogs;

  @Column(nullable = false)
  private boolean hasExperience;

  @Column(nullable = false)
  private int hoursAlonePerDay;

  @Column(nullable = false)
  private String motivation;

  protected ApplicationAnswers() {}

  public ApplicationAnswers(
      Application application,
      HousingType housingType,
      boolean hasYard,
      boolean hasChildrenUnder6,
      boolean hasCats,
      boolean hasDogs,
      boolean hasExperience,
      int hoursAlonePerDay,
      String motivation) {
    this.application = application;
    this.housingType = housingType;
    this.hasYard = hasYard;
    this.hasChildrenUnder6 = hasChildrenUnder6;
    this.hasCats = hasCats;
    this.hasDogs = hasDogs;
    this.hasExperience = hasExperience;
    this.hoursAlonePerDay = hoursAlonePerDay;
    this.motivation = motivation;
  }

  public Long getId() {
    return id;
  }

  public Application getApplication() {
    return application;
  }

  public HousingType getHousingType() {
    return housingType;
  }

  public boolean isHasYard() {
    return hasYard;
  }

  public boolean isHasChildrenUnder6() {
    return hasChildrenUnder6;
  }

  public boolean isHasCats() {
    return hasCats;
  }

  public boolean isHasDogs() {
    return hasDogs;
  }

  public boolean isHasExperience() {
    return hasExperience;
  }

  public int getHoursAlonePerDay() {
    return hoursAlonePerDay;
  }

  public String getMotivation() {
    return motivation;
  }
}
