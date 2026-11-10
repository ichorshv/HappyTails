package com.happytails.adoption.animals.domain;

import com.happytails.adoption.animals.api.AnimalSize;
import com.happytails.adoption.animals.api.AnimalStatus;
import com.happytails.adoption.animals.api.RequirementType;
import com.happytails.adoption.animals.api.Species;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "animals")
public class Animal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Species species;

  @Column(nullable = false)
  private int ageMonths;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AnimalSize size;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private boolean vaccinated;

  @Column(nullable = false)
  private boolean neutered;

  @Column(length = 1000)
  private String medicalNotes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AnimalStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "animal_requirements", joinColumns = @JoinColumn(name = "animal_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private Set<RequirementType> requirements = new HashSet<>();

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected Animal() {}

  public Animal(
      String name,
      Species species,
      int ageMonths,
      AnimalSize size,
      String description,
      boolean vaccinated,
      boolean neutered,
      String medicalNotes,
      Set<RequirementType> requirements,
      Instant createdAt) {
    this.name = name;
    this.species = species;
    this.ageMonths = ageMonths;
    this.size = size;
    this.description = description;
    this.vaccinated = vaccinated;
    this.neutered = neutered;
    this.medicalNotes = medicalNotes;
    this.requirements = new HashSet<>(requirements);
    this.status = AnimalStatus.DRAFT;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Species getSpecies() {
    return species;
  }

  public int getAgeMonths() {
    return ageMonths;
  }

  public AnimalSize getSize() {
    return size;
  }

  public String getDescription() {
    return description;
  }

  public boolean isVaccinated() {
    return vaccinated;
  }

  public boolean isNeutered() {
    return neutered;
  }

  public String getMedicalNotes() {
    return medicalNotes;
  }

  public AnimalStatus getStatus() {
    return status;
  }

  public Set<RequirementType> getRequirements() {
    return Set.copyOf(requirements);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
