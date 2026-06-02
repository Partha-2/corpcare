package com.corpcare.dto;

import com.corpcare.entity.MedicalReport;

import java.time.LocalDateTime;

public class MedicalReportDTO {

    private Long id;
    private Long employeeId;
    private String reportType;
    private String originalFileName;
    private LocalDateTime uploadedAt;
    private String uploadedBy;

    public MedicalReportDTO() {}

    public MedicalReportDTO(Long id, Long employeeId, String reportType, String originalFileName, LocalDateTime uploadedAt, String uploadedBy) {
        this.id = id;
        this.employeeId = employeeId;
        this.reportType = reportType;
        this.originalFileName = originalFileName;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    public static MedicalReportDTO fromEntity(MedicalReport report) {
        return new MedicalReportDTO(
                report.getId(),
                report.getEmployeeId(),
                report.getReportType(),
                report.getOriginalFileName(),
                report.getUploadedAt(),
                report.getUploadedBy()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
