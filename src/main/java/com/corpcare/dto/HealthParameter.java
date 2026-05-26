package com.corpcare.dto;

public class HealthParameter {
    private String name;
    private String value;
    private String unit;
    private String referenceRange;
    private String minRange;
    private String maxRange;
    private String status; // BELOW_RANGE, NORMAL, ABOVE_RANGE
    private String recommendation;
    private String color; // GREEN, YELLOW, RED

    public HealthParameter() {}

    public HealthParameter(String name, String value, String unit, String minRange, String maxRange, String referenceRange) {
        this.name = name;
        this.value = value;
        this.unit = unit;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.referenceRange = referenceRange;
    }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }
    public String getUnit() { return unit; }
    public void setUnit(String v) { this.unit = v; }
    public String getReferenceRange() { return referenceRange; }
    public void setReferenceRange(String v) { this.referenceRange = v; }
    public String getMinRange() { return minRange; }
    public void setMinRange(String v) { this.minRange = v; }
    public String getMaxRange() { return maxRange; }
    public void setMaxRange(String v) { this.maxRange = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String v) { this.recommendation = v; }
    public String getColor() { return color; }
    public void setColor(String v) { this.color = v; }
}
