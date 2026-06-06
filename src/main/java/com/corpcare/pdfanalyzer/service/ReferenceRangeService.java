package com.corpcare.pdfanalyzer.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReferenceRangeService {

    private static final Map<String, double[]> MALE_RANGES = new HashMap<>();
    private static final Map<String, double[]> FEMALE_RANGES = new HashMap<>();
    private static final Map<String, double[]> COMMON_RANGES = new HashMap<>();
    private static final Map<String, String> UNITS = new HashMap<>();
    private static final Map<String, String> CATEGORIES = new HashMap<>();

    static {
        MALE_RANGES.put("haemoglobin", new double[]{13.0, 17.0});
        FEMALE_RANGES.put("haemoglobin", new double[]{12.0, 15.0});
        UNITS.put("haemoglobin", "g/dL");
        CATEGORIES.put("haemoglobin", "BLOOD_CBC");

        MALE_RANGES.put("rbc", new double[]{4.5, 5.5});
        FEMALE_RANGES.put("rbc", new double[]{4.0, 5.0});
        UNITS.put("rbc", "mill/cmm");
        CATEGORIES.put("rbc", "BLOOD_CBC");

        MALE_RANGES.put("hct", new double[]{40.0, 50.0});
        FEMALE_RANGES.put("hct", new double[]{35.0, 47.0});
        UNITS.put("hct", "%");
        CATEGORIES.put("hct", "BLOOD_CBC");

        MALE_RANGES.put("creatinine", new double[]{0.7, 1.2});
        FEMALE_RANGES.put("creatinine", new double[]{0.5, 1.0});
        UNITS.put("creatinine", "mg/dL");
        CATEGORIES.put("creatinine", "BLOOD_CHEMISTRY");

        MALE_RANGES.put("uric acid", new double[]{3.5, 7.2});
        FEMALE_RANGES.put("uric acid", new double[]{2.6, 6.0});
        UNITS.put("uric acid", "mg/dL");
        CATEGORIES.put("uric acid", "BLOOD_CHEMISTRY");

        MALE_RANGES.put("hdl", new double[]{40.0, 60.0});
        FEMALE_RANGES.put("hdl", new double[]{50.0, 70.0});
        UNITS.put("hdl", "mg/dL");
        CATEGORIES.put("hdl", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("wbc", new double[]{4000, 10000});
        UNITS.put("wbc", "cells/cumm");
        CATEGORIES.put("wbc", "BLOOD_CBC");

        COMMON_RANGES.put("platelets", new double[]{150000, 410000});
        UNITS.put("platelets", "cells/cumm");
        CATEGORIES.put("platelets", "BLOOD_CBC");

        COMMON_RANGES.put("mcv", new double[]{83.0, 101.0});
        UNITS.put("mcv", "fL");
        CATEGORIES.put("mcv", "BLOOD_CBC");

        COMMON_RANGES.put("mch", new double[]{27.0, 32.0});
        UNITS.put("mch", "pg");
        CATEGORIES.put("mch", "BLOOD_CBC");

        COMMON_RANGES.put("mchc", new double[]{31.5, 34.5});
        UNITS.put("mchc", "g/dL");
        CATEGORIES.put("mchc", "BLOOD_CBC");

        COMMON_RANGES.put("rdw cv", new double[]{11.6, 14.0});
        UNITS.put("rdw cv", "%");
        CATEGORIES.put("rdw cv", "BLOOD_CBC");

        COMMON_RANGES.put("neutrophils", new double[]{40.0, 80.0});
        UNITS.put("neutrophils", "%");
        CATEGORIES.put("neutrophils", "BLOOD_CBC");

        COMMON_RANGES.put("lymphocytes", new double[]{20.0, 40.0});
        UNITS.put("lymphocytes", "%");
        CATEGORIES.put("lymphocytes", "BLOOD_CBC");

        COMMON_RANGES.put("monocytes", new double[]{2.0, 10.0});
        UNITS.put("monocytes", "%");
        CATEGORIES.put("monocytes", "BLOOD_CBC");

        COMMON_RANGES.put("eosinophils", new double[]{1.0, 6.0});
        UNITS.put("eosinophils", "%");
        CATEGORIES.put("eosinophils", "BLOOD_CBC");

        COMMON_RANGES.put("basophils", new double[]{0.0, 1.0});
        UNITS.put("basophils", "%");
        CATEGORIES.put("basophils", "BLOOD_CBC");

        COMMON_RANGES.put("glucose", new double[]{70.0, 100.0});
        UNITS.put("glucose", "mg/dL");
        CATEGORIES.put("glucose", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("hba1c", new double[]{4.0, 5.6});
        UNITS.put("hba1c", "%");
        CATEGORIES.put("hba1c", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("urea", new double[]{15.0, 45.0});
        UNITS.put("urea", "mg/dL");
        CATEGORIES.put("urea", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("cholesterol", new double[]{0.0, 200.0});
        UNITS.put("cholesterol", "mg/dL");
        CATEGORIES.put("cholesterol", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("triglycerides", new double[]{0.0, 150.0});
        UNITS.put("triglycerides", "mg/dL");
        CATEGORIES.put("triglycerides", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("ldl", new double[]{0.0, 100.0});
        UNITS.put("ldl", "mg/dL");
        CATEGORIES.put("ldl", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("sgot", new double[]{0.0, 40.0});
        UNITS.put("sgot", "U/L");
        CATEGORIES.put("sgot", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("sgpt", new double[]{0.0, 40.0});
        UNITS.put("sgpt", "U/L");
        CATEGORIES.put("sgpt", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("bilirubin", new double[]{0.2, 1.2});
        UNITS.put("bilirubin", "mg/dL");
        CATEGORIES.put("bilirubin", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("protein", new double[]{6.0, 8.3});
        UNITS.put("protein", "g/dL");
        CATEGORIES.put("protein", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("albumin", new double[]{3.5, 5.0});
        UNITS.put("albumin", "g/dL");
        CATEGORIES.put("albumin", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("calcium", new double[]{8.5, 10.5});
        UNITS.put("calcium", "mg/dL");
        CATEGORIES.put("calcium", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("sodium", new double[]{136.0, 145.0});
        UNITS.put("sodium", "mEq/L");
        CATEGORIES.put("sodium", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("potassium", new double[]{3.5, 5.0});
        UNITS.put("potassium", "mEq/L");
        CATEGORIES.put("potassium", "BLOOD_CHEMISTRY");

        COMMON_RANGES.put("tsh", new double[]{0.4, 4.0});
        UNITS.put("tsh", "mIU/L");
        CATEGORIES.put("tsh", "THYROID");

        COMMON_RANGES.put("t3", new double[]{80.0, 200.0});
        UNITS.put("t3", "ng/dL");
        CATEGORIES.put("t3", "THYROID");

        COMMON_RANGES.put("t4", new double[]{5.0, 12.0});
        UNITS.put("t4", "ug/dL");
        CATEGORIES.put("t4", "THYROID");

        COMMON_RANGES.put("free t3", new double[]{2.3, 4.2});
        UNITS.put("free t3", "pg/mL");
        CATEGORIES.put("free t3", "THYROID");

        COMMON_RANGES.put("free t4", new double[]{0.8, 1.8});
        UNITS.put("free t4", "ng/dL");
        CATEGORIES.put("free t4", "THYROID");

        COMMON_RANGES.put("heart rate", new double[]{60.0, 100.0});
        UNITS.put("heart rate", "bpm");
        CATEGORIES.put("heart rate", "ECG");

        COMMON_RANGES.put("vr", new double[]{60.0, 100.0});
        UNITS.put("vr", "bpm");
        CATEGORIES.put("vr", "ECG");

        COMMON_RANGES.put("qrs duration", new double[]{70.0, 100.0});
        UNITS.put("qrs duration", "ms");
        CATEGORIES.put("qrs duration", "ECG");

        COMMON_RANGES.put("qt interval", new double[]{350.0, 440.0});
        UNITS.put("qt interval", "ms");
        CATEGORIES.put("qt interval", "ECG");

        COMMON_RANGES.put("qtcb", new double[]{350.0, 450.0});
        UNITS.put("qtcb", "ms");
        CATEGORIES.put("qtcb", "ECG");

        COMMON_RANGES.put("pri", new double[]{120.0, 200.0});
        UNITS.put("pri", "ms");
        CATEGORIES.put("pri", "ECG");

        COMMON_RANGES.put("urine ph", new double[]{4.6, 8.0});
        UNITS.put("urine ph", "");
        CATEGORIES.put("urine ph", "URINE");

        COMMON_RANGES.put("specific gravity", new double[]{1.003, 1.035});
        UNITS.put("specific gravity", "");
        CATEGORIES.put("specific gravity", "URINE");

        COMMON_RANGES.put("height", new double[]{150.0, 190.0});
        UNITS.put("height", "cm");
        CATEGORIES.put("height", "MER");

        COMMON_RANGES.put("weight", new double[]{40.0, 100.0});
        UNITS.put("weight", "kg");
        CATEGORIES.put("weight", "MER");

        COMMON_RANGES.put("pulse", new double[]{60.0, 100.0});
        UNITS.put("pulse", "bpm");
        CATEGORIES.put("pulse", "MER");
    }

    public double[] getRange(String param, String gender) {
        String key = param.toLowerCase().trim();
        if ("MALE".equalsIgnoreCase(gender) && MALE_RANGES.containsKey(key))
            return MALE_RANGES.get(key);
        if ("FEMALE".equalsIgnoreCase(gender) && FEMALE_RANGES.containsKey(key))
            return FEMALE_RANGES.get(key);
        return COMMON_RANGES.get(key);
    }

    public String getStatus(String param, double value, String gender) {
        double[] range = getRange(param, gender);
        if (range == null) return "N/A";
        if (value < range[0]) return "LOW";
        if (value > range[1]) return "HIGH";
        return "NORMAL";
    }

    public String getBPStatus(String bpValue) {
        if (bpValue == null || !bpValue.contains("/")) return "INFO";
        try {
            String[] parts = bpValue.split("/");
            int sys = Integer.parseInt(parts[0].trim());
            int dia = Integer.parseInt(parts[1].trim());
            if (sys >= 140 || dia >= 90) return "HIGH";
            if (sys >= 130 || dia >= 80) return "HIGH";
            if (sys > 120)               return "HIGH";
            return "NORMAL";
        } catch (Exception e) {
            return "INFO";
        }
    }

    public String getBPRange() {
        return "90/60 - 120/80";
    }

    public String getUnit(String param) {
        return UNITS.getOrDefault(param.toLowerCase().trim(), "");
    }

    public String getCategory(String param) {
        return CATEGORIES.getOrDefault(param.toLowerCase().trim(), "OTHER");
    }

    public String formatRange(double[] range) {
        if (range == null) return "N/A";
        return range[0] + " - " + range[1];
    }
}
