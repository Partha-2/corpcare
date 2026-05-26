package com.corpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_detail")
public class ReportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    private String fileName;
    private String vendor;
    private String confidence;
    private int parsedCount;

    private String patientName;
    private String patientAge;
    private String patientSex;
    private String patientDate;

    private Double haemoglobin; private String haemoglobinStatus;
    private Double rbcCount; private String rbcCountStatus;
    private Double pcvHct; private String pcvHctStatus;
    private Double mcv; private String mcvStatus;
    private Double mch; private String mchStatus;
    private Double mchc; private String mchcStatus;
    private Double rdwCv; private String rdwCvStatus;
    private Double totalWbcCount; private String totalWbcCountStatus;
    private Double neutrophils; private String neutrophilsStatus;
    private Double lymphocytes; private String lymphocytesStatus;
    private Double monocytes; private String monocytesStatus;
    private Double eosinophils; private String eosinophilsStatus;
    private Double basophils; private String basophilsStatus;
    private Double plateletCount; private String plateletCountStatus;
    private Double esr; private String esrStatus;
    private Double creatinine; private String creatinineStatus;
    private Double urinePusCells; private String urinePusCellsStatus;
    private String urineProtein; private String urineProteinStatus;
    private Double urineSugar; private String urineSugarStatus;
    private Double urineRbc; private String urineRbcStatus;

    private String criticalAlert;
    private String criticalAlertMessage;

    @Column(columnDefinition = "TEXT")
    private String rawJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ReportDetail() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long v) { this.employeeId = v; }
    public String getFileName() { return fileName; }
    public void setFileName(String v) { this.fileName = v; }
    public String getVendor() { return vendor; }
    public void setVendor(String v) { this.vendor = v; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String v) { this.confidence = v; }
    public int getParsedCount() { return parsedCount; }
    public void setParsedCount(int v) { this.parsedCount = v; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String v) { this.patientName = v; }
    public String getPatientAge() { return patientAge; }
    public void setPatientAge(String v) { this.patientAge = v; }
    public String getPatientSex() { return patientSex; }
    public void setPatientSex(String v) { this.patientSex = v; }
    public String getPatientDate() { return patientDate; }
    public void setPatientDate(String v) { this.patientDate = v; }

    public Double getHaemoglobin() { return haemoglobin; } public void setHaemoglobin(Double v) { this.haemoglobin = v; }
    public String getHaemoglobinStatus() { return haemoglobinStatus; } public void setHaemoglobinStatus(String v) { this.haemoglobinStatus = v; }
    public Double getRbcCount() { return rbcCount; } public void setRbcCount(Double v) { this.rbcCount = v; }
    public String getRbcCountStatus() { return rbcCountStatus; } public void setRbcCountStatus(String v) { this.rbcCountStatus = v; }
    public Double getPcvHct() { return pcvHct; } public void setPcvHct(Double v) { this.pcvHct = v; }
    public String getPcvHctStatus() { return pcvHctStatus; } public void setPcvHctStatus(String v) { this.pcvHctStatus = v; }
    public Double getMcv() { return mcv; } public void setMcv(Double v) { this.mcv = v; }
    public String getMcvStatus() { return mcvStatus; } public void setMcvStatus(String v) { this.mcvStatus = v; }
    public Double getMch() { return mch; } public void setMch(Double v) { this.mch = v; }
    public String getMchStatus() { return mchStatus; } public void setMchStatus(String v) { this.mchStatus = v; }
    public Double getMchc() { return mchc; } public void setMchc(Double v) { this.mchc = v; }
    public String getMchcStatus() { return mchcStatus; } public void setMchcStatus(String v) { this.mchcStatus = v; }
    public Double getRdwCv() { return rdwCv; } public void setRdwCv(Double v) { this.rdwCv = v; }
    public String getRdwCvStatus() { return rdwCvStatus; } public void setRdwCvStatus(String v) { this.rdwCvStatus = v; }
    public Double getTotalWbcCount() { return totalWbcCount; } public void setTotalWbcCount(Double v) { this.totalWbcCount = v; }
    public String getTotalWbcCountStatus() { return totalWbcCountStatus; } public void setTotalWbcCountStatus(String v) { this.totalWbcCountStatus = v; }
    public Double getNeutrophils() { return neutrophils; } public void setNeutrophils(Double v) { this.neutrophils = v; }
    public String getNeutrophilsStatus() { return neutrophilsStatus; } public void setNeutrophilsStatus(String v) { this.neutrophilsStatus = v; }
    public Double getLymphocytes() { return lymphocytes; } public void setLymphocytes(Double v) { this.lymphocytes = v; }
    public String getLymphocytesStatus() { return lymphocytesStatus; } public void setLymphocytesStatus(String v) { this.lymphocytesStatus = v; }
    public Double getMonocytes() { return monocytes; } public void setMonocytes(Double v) { this.monocytes = v; }
    public String getMonocytesStatus() { return monocytesStatus; } public void setMonocytesStatus(String v) { this.monocytesStatus = v; }
    public Double getEosinophils() { return eosinophils; } public void setEosinophils(Double v) { this.eosinophils = v; }
    public String getEosinophilsStatus() { return eosinophilsStatus; } public void setEosinophilsStatus(String v) { this.eosinophilsStatus = v; }
    public Double getBasophils() { return basophils; } public void setBasophils(Double v) { this.basophils = v; }
    public String getBasophilsStatus() { return basophilsStatus; } public void setBasophilsStatus(String v) { this.basophilsStatus = v; }
    public Double getPlateletCount() { return plateletCount; } public void setPlateletCount(Double v) { this.plateletCount = v; }
    public String getPlateletCountStatus() { return plateletCountStatus; } public void setPlateletCountStatus(String v) { this.plateletCountStatus = v; }
    public Double getEsr() { return esr; } public void setEsr(Double v) { this.esr = v; }
    public String getEsrStatus() { return esrStatus; } public void setEsrStatus(String v) { this.esrStatus = v; }
    public Double getCreatinine() { return creatinine; } public void setCreatinine(Double v) { this.creatinine = v; }
    public String getCreatinineStatus() { return creatinineStatus; } public void setCreatinineStatus(String v) { this.creatinineStatus = v; }
    public Double getUrinePusCells() { return urinePusCells; } public void setUrinePusCells(Double v) { this.urinePusCells = v; }
    public String getUrinePusCellsStatus() { return urinePusCellsStatus; } public void setUrinePusCellsStatus(String v) { this.urinePusCellsStatus = v; }
    public String getUrineProtein() { return urineProtein; } public void setUrineProtein(String v) { this.urineProtein = v; }
    public String getUrineProteinStatus() { return urineProteinStatus; } public void setUrineProteinStatus(String v) { this.urineProteinStatus = v; }
    public Double getUrineSugar() { return urineSugar; } public void setUrineSugar(Double v) { this.urineSugar = v; }
    public String getUrineSugarStatus() { return urineSugarStatus; } public void setUrineSugarStatus(String v) { this.urineSugarStatus = v; }
    public Double getUrineRbc() { return urineRbc; } public void setUrineRbc(Double v) { this.urineRbc = v; }
    public String getUrineRbcStatus() { return urineRbcStatus; } public void setUrineRbcStatus(String v) { this.urineRbcStatus = v; }

    public String getCriticalAlert() { return criticalAlert; }
    public void setCriticalAlert(String v) { this.criticalAlert = v; }
    public String getCriticalAlertMessage() { return criticalAlertMessage; }
    public void setCriticalAlertMessage(String v) { this.criticalAlertMessage = v; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String v) { this.rawJson = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
