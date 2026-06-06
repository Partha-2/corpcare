package com.corpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyzed_reports")
public class AnalyzedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reportId;

    @Column(nullable = false, length = 10)
    private String patientId;

    @Column(nullable = false, length = 20)
    private String reportType;

    private String reportDate;
    private String sourceType;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String parametersJson;

    @Column(columnDefinition = "bytea")
    private byte[] pdfData;

    private int totalParameters;
    private int highCount;
    private int lowCount;
    private int normalCount;
    private int unknownCount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public AnalyzedReport() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getReportId() { return reportId; }
    public void setReportId(String v) { this.reportId = v; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String v) { this.patientId = v; }
    public String getReportType() { return reportType; }
    public void setReportType(String v) { this.reportType = v; }
    public String getReportDate() { return reportDate; }
    public void setReportDate(String v) { this.reportDate = v; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String v) { this.sourceType = v; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String v) { this.diagnosis = v; }
    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String v) { this.parametersJson = v; }
    public byte[] getPdfData() { return pdfData; }
    public void setPdfData(byte[] v) { this.pdfData = v; }
    public int getTotalParameters() { return totalParameters; }
    public void setTotalParameters(int v) { this.totalParameters = v; }
    public int getHighCount() { return highCount; }
    public void setHighCount(int v) { this.highCount = v; }
    public int getLowCount() { return lowCount; }
    public void setLowCount(int v) { this.lowCount = v; }
    public int getNormalCount() { return normalCount; }
    public void setNormalCount(int v) { this.normalCount = v; }
    public int getUnknownCount() { return unknownCount; }
    public void setUnknownCount(int v) { this.unknownCount = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
