package com.corpcare.service;

import com.corpcare.dto.HealthParameter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class HealthRangeValidator {

    private static final Map<String, RangeDef> RANGES = new LinkedHashMap<>();

    static {
        RANGES.put("hemoglobin_male", new RangeDef(13.5, 17.5, "g/dL"));
        RANGES.put("hemoglobin_female", new RangeDef(12.0, 15.5, "g/dL"));
        RANGES.put("rbcCount_male", new RangeDef(4.5, 5.5, "M/μL"));
        RANGES.put("rbcCount_female", new RangeDef(4.0, 5.0, "M/μL"));
        RANGES.put("wbcCount", new RangeDef(4000, 11000, "/μL"));
        RANGES.put("plateletCount", new RangeDef(1.5, 4.5, "L/μL"));
        RANGES.put("esr_male", new RangeDef(0, 22, "mm/hr"));
        RANGES.put("esr_female", new RangeDef(0, 29, "mm/hr"));
        RANGES.put("creatinine_male", new RangeDef(0.7, 1.3, "mg/dL"));
        RANGES.put("creatinine_female", new RangeDef(0.6, 1.1, "mg/dL"));
        RANGES.put("urea", new RangeDef(17, 43, "mg/dL"));
        RANGES.put("bloodSugar", new RangeDef(70, 110, "mg/dL"));
        RANGES.put("sgpt", new RangeDef(7, 56, "U/L"));
        RANGES.put("sgot", new RangeDef(10, 40, "U/L"));
        RANGES.put("urinePH", new RangeDef(4.5, 8.0, ""));
        RANGES.put("specificGravity", new RangeDef(1.005, 1.030, ""));
        RANGES.put("urineProtein", new RangeDef(0, 15, "mg/dL"));
        RANGES.put("urineGlucose", new RangeDef(0, 15, "mg/dL"));
        RANGES.put("pcv", new RangeDef(37, 53, "%"));
        RANGES.put("mcv", new RangeDef(80, 100, "fL"));
    }

    public HealthParameter validate(String paramName, String rawValue, boolean isMale) {
        HealthParameter hp = new HealthParameter();
        hp.setName(formatParamName(paramName));

        if ("Not Available".equals(rawValue) || rawValue == null || rawValue.isBlank()) {
            hp.setValue("Not Available");
            hp.setStatus("NOT_AVAILABLE");
            hp.setColor("GRAY");
            hp.setRecommendation("N/A");
            return hp;
        }

        String cleaned = cleanNumeric(rawValue);
        hp.setValue(cleaned);

        String key = paramName + (isMale ? "_male" : "_female");
        RangeDef range = RANGES.get(key);
        if (range == null) {
            range = RANGES.get(paramName);
        }

        boolean isQualitative = "urineProtein".equals(paramName) || "urineGlucose".equals(paramName);

        if (isQualitative) {
            hp.setUnit("mg/dL");
            String lower = cleaned.toLowerCase();
            if ("nil".equals(lower) || "absent".equals(lower) || "negative".equals(lower) || "normal".equals(lower)) {
                hp.setStatus("NORMAL");
                hp.setColor("GREEN");
                hp.setRecommendation("Normal");
            } else {
                hp.setStatus("ABOVE_RANGE");
                hp.setColor("RED");
                hp.setRecommendation("Abnormal value detected. Please consult a doctor.");
            }
            return hp;
        }

        if (range == null) {
            hp.setStatus("NORMAL");
            hp.setColor("GREEN");
            hp.setRecommendation("N/A");
            return hp;
        }

        hp.setUnit(range.unit);
        hp.setMinRange(String.valueOf(range.min));
        hp.setMaxRange(String.valueOf(range.max));
        hp.setReferenceRange(range.min + " - " + range.max);

        try {
            double val = Double.parseDouble(cleaned);
            if (val < range.min) {
                hp.setStatus("BELOW_RANGE");
                hp.setColor("YELLOW");
                hp.setRecommendation("Correction Needed: Increase to minimum " + range.min);
            } else if (val > range.max) {
                hp.setStatus("ABOVE_RANGE");
                hp.setColor("RED");
                hp.setRecommendation("NOT POSSIBLE - Exceeds Healthy Limit. Immediate medical attention recommended.");
            } else {
                hp.setStatus("NORMAL");
                hp.setColor("GREEN");
                hp.setRecommendation("Within healthy range");
            }
        } catch (NumberFormatException e) {
            hp.setStatus("NORMAL");
            hp.setColor("GREEN");
            hp.setRecommendation("N/A");
        }

        return hp;
    }

    private double parseNumeric(String val) {
        if (val == null || val.isBlank()) throw new NumberFormatException();
        val = val.trim().replaceAll("[^\\d.]", "");
        return Double.parseDouble(val);
    }

    private String cleanNumeric(String val) {
        if (val == null || val.isBlank()) return val;
        return val.trim().replaceAll("[^\\d.A-Za-z+\\-]", " ").trim().split("\\s+")[0];
    }

    private String formatParamName(String key) {
        return switch (key) {
            case "employeeName" -> "Employee Name";
            case "bloodGroup" -> "Blood Group";
            case "rbcCount" -> "RBC Count";
            case "wbcCount" -> "WBC Count";
            case "plateletCount" -> "Platelet Count";
            case "esr" -> "ESR";
            case "creatinine" -> "Creatinine";
            case "bloodSugar" -> "Blood Sugar";
            case "sgpt" -> "SGPT (ALT)";
            case "sgot" -> "SGOT (AST)";
            case "urinePH" -> "Urine pH";
            case "specificGravity" -> "Specific Gravity";
            case "urineProtein" -> "Protein (Urine)";
            case "urineGlucose" -> "Glucose (Urine)";
            case "pcv" -> "PCV/HCT";
            case "mcv" -> "MCV";
            default -> key.substring(0, 1).toUpperCase() + key.substring(1);
        };
    }

    private record RangeDef(double min, double max, String unit) {}
}
