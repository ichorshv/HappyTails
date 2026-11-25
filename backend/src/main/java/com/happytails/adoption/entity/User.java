package com.happytails.adoption.entity;

import com.happytails.adoption.enums.Role;
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
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 100)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  private boolean active;

  /** Only applicants have these; they enter them at registration. */
  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String municipality;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  protected User() {}

  public User(String email, String passwordHash, String fullName, Role role, Instant createdAt) {
    this.email = email;
    this.passwordHash = passwordHash;
    this.fullName = fullName;
    this.role = role;
    this.active = true;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getFullName() {
    return fullName;
  }

  public Role getRole() {
    return role;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getCity() {
    return city;
  }

  public String getMunicipality() {
    return municipality;
  }

  public void setLocation(String city, String municipality) {
    this.city = city;
    this.municipality = municipality;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
