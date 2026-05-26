package com.corpcare.dto;

import java.util.ArrayList;
import java.util.List;

public class ReportAnalysisResult {

    private String vendor;
    private String confidence;
    private int parsedCount;
    private PatientInfo patient;
    private List<ParameterResult> parameters = new ArrayList<>();
    private List<AlertItem> alerts = new ArrayList<>();

    public String getVendor() { return vendor; }
    public void setVendor(String v) { this.vendor = v; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String v) { this.confidence = v; }
    public int getParsedCount() { return parsedCount; }
    public void setParsedCount(int v) { this.parsedCount = v; }
    public PatientInfo getPatient() { return patient; }
    public void setPatient(PatientInfo v) { this.patient = v; }
    public List<ParameterResult> getParameters() { return parameters; }
    public void setParameters(List<ParameterResult> v) { this.parameters = v; }
    public List<AlertItem> getAlerts() { return alerts; }
    public void setAlerts(List<AlertItem> v) { this.alerts = v; }

    public static class PatientInfo {
        private String name = "";
        private String age = "";
        private String sex = "";
        private String date = "";

        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getAge() { return age; }
        public void setAge(String v) { this.age = v; }
        public String getSex() { return sex; }
        public void setSex(String v) { this.sex = v; }
        public String getDate() { return date; }
        public void setDate(String v) { this.date = v; }
    }

    public static class ParameterResult {
        private String name;
        private String value;
        private String unit;
        private Double rangeMin;
        private Double rangeMax;
        private String status; // NORMAL, HIGH, LOW, ABNORMAL, NOT_FOUND
        private String rawText;

        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
        public String getUnit() { return unit; }
        public void setUnit(String v) { this.unit = v; }
        public Double getRangeMin() { return rangeMin; }
        public void setRangeMin(Double v) { this.rangeMin = v; }
        public Double getRangeMax() { return rangeMax; }
        public void setRangeMax(Double v) { this.rangeMax = v; }
        public String getStatus() { return status; }
        public void setStatus(String v) { this.status = v; }
        public String getRawText() { return rawText; }
        public void setRawText(String v) { this.rawText = v; }
    }

    public static class AlertItem {
        private String parameter;
        private String value;
        private String range;
        private String direction;
        private String message;

        public String getParameter() { return parameter; }
        public void setParameter(String v) { this.parameter = v; }
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
        public String getRange() { return range; }
        public void setRange(String v) { this.range = v; }
        public String getDirection() { return direction; }
        public void setDirection(String v) { this.direction = v; }
        public String getMessage() { return message; }
        public void setMessage(String v) { this.message = v; }
    }
}
