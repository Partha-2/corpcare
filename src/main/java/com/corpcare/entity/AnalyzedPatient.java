package com.corpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyzed_patients")
public class AnalyzedPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String patientId;

    @Column(nullable = false, length = 60)
    private String patientName;

    @Column(nullable = false, length = 6)
    private String gender;

    private int age;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public AnalyzedPatient() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String v) { this.patientId = v; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public int getAge() { return age; }
    public void setAge(int v) { this.age = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
