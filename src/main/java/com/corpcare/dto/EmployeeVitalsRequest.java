package com.corpcare.dto;

import com.corpcare.enums.BloodGroup;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

public class EmployeeVitalsRequest {
    @Positive
    @DecimalMin(value = "100", message = "Height must be at least 100 cm")
    @DecimalMax(value = "250", message = "Height cannot exceed 250 cm")
    private Double height;

    @Positive
    @DecimalMin(value = "30", message = "Weight must be at least 30 kg")
    @DecimalMax(value = "150", message = "Weight cannot exceed 150 kg")
    private Double weight;

    private String bloodPressure;

    @Positive
    private Double bloodSugar;

    private BloodGroup bloodGroup;

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
}
