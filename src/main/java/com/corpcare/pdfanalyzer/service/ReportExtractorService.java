package com.corpcare.pdfanalyzer.service;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportExtractorService {

    public String detectType(String text) {
        if (text.contains("ecg") || text.contains("qrs") || text.contains("sinus"))
            return "ECG";
        if (text.contains("haemoglobin") || text.contains("hemoglobin")
                || text.contains("wbc") || text.contains("platelet")
                || text.contains("tlc") || text.contains("leucocyte"))
            return "BLOOD";
        if (text.contains("tsh") || text.contains("thyroid") || text.contains("free t3"))
            return "THYROID";
        if (text.contains("specific gravity")
                || (text.contains("urine") && text.contains("microscopy")))
            return "URINE";
        if (text.contains("xray") || text.contains("x-ray")
                || text.contains("lung fields") || text.contains("chest"))
            return "XRAY";
        return "GENERAL";
    }

    public String extractDiagnosis(String text, String type) {
        if ("ECG".equals(type)) {
            Matcher m = Pattern.compile(
                    "((?:ecg|cg)\\s+within[^'\"\\|\\n]{0,150})",
                    Pattern.CASE_INSENSITIVE).matcher(text);
            if (m.find()) return m.group(1).trim();
        }
        Matcher m = Pattern.compile(
                "(?:impression|conclusion|diagnosis)[:\\s]+([^\\n]{5,120})",
                Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    public Map<String, String> extractValues(String text) {
        Map<String, String> values = new LinkedHashMap<>();

        if (text.contains("haemoglobin") || text.contains("hemoglobin")
                || text.contains("tlc") || text.contains("leucocyte")
                || text.contains("platelet"))
            runPatterns(BLOOD_PATTERNS, text, values);

        if (text.contains("specific gravity") || text.contains("urine")
                || text.contains("microscopy"))
            runPatterns(URINE_PATTERNS, text, values);

        if (text.contains("lung") || text.contains("chest x-ray")
                || text.contains("trachea") || text.contains("costophrenic"))
            runPatterns(XRAY_PATTERNS, text, values);

        if (text.contains("tsh") || text.contains("thyroid"))
            runPatterns(THYROID_PATTERNS, text, values);

        runPatterns(GLUCOSE_PATTERNS, text, values);

        if (text.contains("eye") || text.contains("vision"))
            runPatterns(EYE_PATTERNS, text, values);

        if (text.contains("height") || text.contains("weight")
                || text.contains("pulse") || text.contains("bpm"))
            runPatterns(MER_PATTERNS, text, values);

        return values;
    }

    private void runPatterns(String[][] patterns, String text, Map<String, String> values) {
        for (String[] pat : patterns) {
            if (values.containsKey(pat[0])) continue;
            try {
                Matcher m = Pattern.compile(pat[1],
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(text);
                if (m.find()) {
                    String g1 = m.groupCount() >= 1 && m.group(1) != null ? m.group(1).trim() : "";
                    String g2 = m.groupCount() >= 2 && m.group(2) != null ? m.group(2).trim() : "";
                    String raw = (!g1.isEmpty() ? g1 : g2).replace(",", "");
                    if (raw.isEmpty()) continue;

                    String val = raw;

                    if ("Platelets".equals(pat[0])) {
                        try {
                            double num = Double.parseDouble(raw);
                            if (num < 100) {
                                val = String.valueOf(Math.round(num * 100000));
                            }
                        } catch (NumberFormatException ignored) {}
                    }

                    if (val.matches("[0-9.,\\s]+"))
                        val = val.replace(" ", "");

                    values.put(pat[0], val);
                }
            } catch (Exception ignored) {}
        }
    }

    private static final String[][] BLOOD_PATTERNS = {
        {"Haemoglobin",  "h(?:ae|e)moglobin[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"RBC",          "rbc[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"WBC",          "(?:tlc|total leucocyte)[^\\n]*\\n[^\\n]*\\n([0-9,]+)"},
        {"Platelets",    "platelet[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"HCT",          "(?:packed cell volume|pcv|hct)[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"MCV",          "\\bmcv\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"MCH",          "\\bmch\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"MCHC",         "\\bmchc\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"RDW CV",       "rdw[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Neutrophils",  "neutro[a-z]*\\s+([0-9]+)\\s+%|neutro[a-z]*\\s+[0-9]+\\s*-\\s*[0-9]+%([0-9]+)"},
        {"Lymphocytes",  "lympho[a-z]*\\s+([0-9]+)\\s+%|lympho[a-z]*\\s+[0-9]+\\s*-\\s*[0-9]+%([0-9]+)"},
        {"Monocytes",    "mono[a-z]*\\s+([0-9]+)\\s+%|mono[a-z]*\\s+[0-9]+\\s*-\\s*[0-9]+%([0-9]+)"},
        {"Eosinophils",  "eosino[a-z]*\\s+([0-9]+)\\s+%|eosino[a-z]*\\s+[0-9]+\\s*-\\s*[0-9]+%([0-9]+)"},
        {"Basophils",    "baso[a-z]*\\s+([0-9]+)\\s+%|baso[a-z]*\\s+[0-9]+\\s*-\\s*[0-9]+%([0-9]+)"},
        {"HbA1c",        "hba1c[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Urea",         "\\burea\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Creatinine",   "creatinine[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Uric Acid",    "uric\\s+acid[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Cholesterol",  "(?:total\\s+)?cholesterol[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Triglycerides","triglyceride[a-z]*[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"HDL",          "\\bhdl\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"LDL",          "\\bldl\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"SGOT",         "(?:sgot|\\bast\\b)[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"SGPT",         "(?:sgpt|\\balt\\b)[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Bilirubin",    "(?:total\\s+)?bilirubin[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Protein",      "(?:total\\s+)?protein[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Albumin",      "\\balbumin\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Sodium",       "\\bsodium\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Potassium",    "\\bpotassium\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Calcium",      "\\bcalcium\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
    };

    private static final String[][] GLUCOSE_PATTERNS = {
        {"Glucose", "glucose[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
    };

    private static final String[][] THYROID_PATTERNS = {
        {"TSH",    "\\btsh\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"T3",     "\\bt3\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"T4",     "\\bt4\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Free T3","free\\s+t3[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Free T4","free\\s+t4[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
    };

    private static final String[][] URINE_PATTERNS = {
        {"Urine pH",         "\\bph\\b[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Specific Gravity", "specific\\s+gravity[^\\n]*\\n[^\\n]*\\n([0-9]+\\.?[0-9]*)"},
        {"Urine Protein",    "protein[^\\n]*\\n[^\\n]*\\n(nil|negative|absent|trace|\\+{1,3})"},
        {"Urine Glucose",    "glucose[^\\n]*\\n[^\\n]*\\n(nil|negative|absent|normal|trace)"},
    };

    private static final String[][] XRAY_PATTERNS = {
        {"Lung Fields",  "(?:bilateral\\s+)?lung\\s+fields?([^\\n]{5,80})"},
        {"Heart",        "(?:heart|cardiac)\\s+shadow([^\\n]{5,80})"},
        {"Trachea",      "trachea([^\\n]{5,80})"},
        {"Costophrenic", "costophrenic([^\\n]{5,80})"},
    };

    private static final String[][] EYE_PATTERNS = {
        {"Near Vision",    "near[\\s\\t]+vision[^\\n]*([n][/][0-9]+)"},
        {"Distance Vision","distance[\\s\\t\\n]+vision[^\\n]*([0-9]+[/][0-9]+)"},
        {"Color Vision",   "color[\\s\\t]*vision[\\s\\t\\n]*(normal)"},
    };

    private static final String[][] MER_PATTERNS = {
        {"Height",         "height[^\\n]*?([0-9]{2,3})\\s*(?:cm)?"},
        {"Weight",         "weight[^\\n]*?([0-9]{2,3})\\s*(?:kg)?"},
        {"Pulse",          "pulse[^\\n]*?([0-9]{2,3})\\s*(?:bpm)?"},
        {"Blood Pressure", "bp[^\\n]*?([0-9]{2,3}/[0-9]{2,3})"},
    };
}
