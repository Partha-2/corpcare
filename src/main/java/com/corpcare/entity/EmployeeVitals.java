package com.corpcare.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.corpcare.enums.BloodGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_vitals")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmployeeVitals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Positive
    @DecimalMin("20") @DecimalMax("300")
    @Column(nullable = false)
    private Double height;

    @Positive
    @DecimalMin("1") @DecimalMax("500")
    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private String bloodPressure;

    @Positive
    @Column(nullable = false)
    private Double bloodSugar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }

    public EmployeeVitals() {}

    public EmployeeVitals(Employee employee, Double height, Double weight,
                          String bloodPressure, Double bloodSugar, BloodGroup bloodGroup) {
        this.employee = employee;
        this.height = height;
        this.weight = weight;
        this.bloodPressure = bloodPressure;
        this.bloodSugar = bloodSugar;
        this.bloodGroup = bloodGroup;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public Double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Double bloodSugar) { this.bloodSugar = bloodSugar; }
    public BloodGroup getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(BloodGroup bloodGroup) { this.bloodGroup = bloodGroup; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
