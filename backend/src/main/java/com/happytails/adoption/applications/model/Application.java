package com.happytails.adoption.applications.model;

import com.happytails.adoption.queue.api.ApplicationStatus;
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
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicantId;

    @Column(nullable = false)
    private Long animalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(nullable = false, length = 200)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String municipality;

    private Double latitude;

    private Double longitude;

    private Double distanceKm;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant submittedAt;

    protected Application() {}

    public Application(Long applicantId, Long animalId, String street, String city,
                       String municipality, Instant submittedAt) {
        this.applicantId = applicantId;
        this.animalId = animalId;
        this.street = street;
        this.city = city;
        this.municipality = municipality;
        this.submittedAt = submittedAt;
        this.status = ApplicationStatus.SUBMITTED;
    }

    public Long getId() {
        return id;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public Long getAnimalId() {
        return animalId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getMunicipality() {
        return municipality;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public void setLocation(double latitude, double longitude, double distanceKm) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
    }
}