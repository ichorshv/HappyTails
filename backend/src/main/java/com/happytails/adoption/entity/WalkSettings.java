package com.happytails.adoption.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "walk_settings")
public class WalkSettings {

  @Id private Long id;

  @Column(name = "work_start", nullable = false)
  private LocalTime workStart;

  @Column(name = "work_end", nullable = false)
  private LocalTime workEnd;

  @Column(name = "slot_minutes", nullable = false)
  private int slotMinutes;

  @Column(name = "staff_count", nullable = false)
  private int staffCount;

  protected WalkSettings() {}

  public WalkSettings(
      Long id, LocalTime workStart, LocalTime workEnd, int slotMinutes, int staffCount) {
    this.id = id;
    this.workStart = workStart;
    this.workEnd = workEnd;
    this.slotMinutes = slotMinutes;
    this.staffCount = staffCount;
  }

  public Long getId() {
    return id;
  }

  public LocalTime getWorkStart() {
    return workStart;
  }

  public void setWorkStart(LocalTime workStart) {
    this.workStart = workStart;
  }

  public LocalTime getWorkEnd() {
    return workEnd;
  }

  public void setWorkEnd(LocalTime workEnd) {
    this.workEnd = workEnd;
  }

  public int getSlotMinutes() {
    return slotMinutes;
  }

  public void setSlotMinutes(int slotMinutes) {
    this.slotMinutes = slotMinutes;
  }

  public int getStaffCount() {
    return staffCount;
  }

  public void setStaffCount(int staffCount) {
    this.staffCount = staffCount;
  }
}
