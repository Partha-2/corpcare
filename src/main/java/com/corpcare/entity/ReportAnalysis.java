package com.corpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_analysis")
public class ReportAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    private String fileName;
    private String vendor;
    private String patientName;
    private String patientAge;
    private String patientSex;
    private String patientDate;
    private int parsedCount;
    private String confidence;

    @Column(columnDefinition = "TEXT")
    private String reportData;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ReportAnalysis() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long v) { this.employeeId = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { this.fileName = v; }
    public String getVendor() { return vendor; }
    public void setVendor(String v) { this.vendor = v; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }
    public String getPatientAge() { return patientAge; }
    public void setPatientAge(String v) { this.patientAge = v; }
    public String getPatientSex() { return patientSex; }
    public void setPatientSex(String v) { this.patientSex = v; }
    public String getPatientDate() { return patientDate; }
    public void setPatientDate(String v) { this.patientDate = v; }
    public int getParsedCount() { return parsedCount; }
    public void setParsedCount(int v) { this.parsedCount = v; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String v) { this.confidence = v; }
    public String getReportData() { return reportData; }
    public void setReportData(String v) { this.reportData = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
