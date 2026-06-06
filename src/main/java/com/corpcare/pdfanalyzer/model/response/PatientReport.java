package com.corpcare.pdfanalyzer.model.response;

import lombok.Data;
import java.util.List;

@Data
public class PatientReport {
    private String patientId;
    private String patientName;
    private String gender;
    private int age;

    private String reportId;
    private String reportType;
    private String reportDate;
    private String sourceType;

    private String diagnosis;
    private List<ReportParameter> parameters;

    private int totalParameters;
    private int highCount;
    private int lowCount;
    private int normalCount;
    private int unknownCount;

    private String jsonFile;
    private String pdfFile;

    private String viewUrl;
    private String downloadUrl;
    private String deleteUrl;
}
