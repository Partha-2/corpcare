package com.corpcare.service;

import com.corpcare.dto.ReportAnalysisResult;
import com.corpcare.dto.ReportAnalysisResult.AlertItem;
import com.corpcare.dto.ReportAnalysisResult.ParameterResult;
import com.corpcare.dto.ReportAnalysisResult.PatientInfo;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(ReportAnalyzerService.class);

    private static class PatternSet {
        final List<Pattern> patterns;
        final String unit;
        final Double min;
        final Double max;
        final boolean qualitative;
        final String name;

        PatternSet(String name, String unit, Double min, Double max, String... regexes) {
            this.name = name;
            this.unit = unit;
            this.min = min;
            this.max = max;
            this.qualitative = false;
            this.patterns = new ArrayList<>();
            for (String r : regexes) {
                this.patterns.add(Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
            }
        }

        PatternSet(String name, String unit, String... regexes) {
            this.name = name;
            this.unit = unit;
            this.min = null;
            this.max = null;
            this.qualitative = true;
            this.patterns = new ArrayList<>();
            for (String r : regexes) {
                this.patterns.add(Pattern.compile(r, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
            }
        }
    }

    private static final String NUM = "(\\d+(?:[.,]\\d+)?)";
    private static final String QUAL = "(Nil|Absent|Negative|Normal|Present|Positive|Non-Reactive|\\d+)";
    private static final String RANGE = "\\d+(?:[.,]\\d+)?\\s*[-\\u2013]\\s*\\d+(?:[.,]\\d+)?[^\\d]*?";

    private static final List<PatternSet> PARAMS = List.of(
        new PatternSet("Haemoglobin", "g/dL", 13.5, 17.5,
            "(?:haemoglobin|hemoglobin|hb)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*(?:haemoglobin|hemoglobin|hb)\\b",
            RANGE + NUM + "[ \\t]*(?:haemoglobin|hemoglobin|hb)\\b"),
        new PatternSet("RBC Count", "milli./cu.mm", 4.5, 5.9,
            "(?:rbc\\s*count|rbcs?|red\\s*blood\\s*cells?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*(?:rbc|rbcs?)\\b",
            RANGE + NUM + "[ \\t]*(?:rbc|rbcs?|red\\s*blood\\s*cells?)\\b"),
        new PatternSet("PCV / HCT", "%", 37.0, 53.0,
            "(?:packed\\s*cell\\s*volume|pcv|hct)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*(?:pcv|hct|packed\\s*cell\\s*volume)\\b",
            RANGE + NUM + "[ \\t]*(?:pcv|hct)\\b"),
        new PatternSet("MCV", "fL", 80.0, 100.0,
            "(?:mean\\s*corpuscular\\s*volume|mcv)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*mcv\\b",
            RANGE + NUM + "[ \\t]*mcv\\b"),
        new PatternSet("MCH", "pg", 26.0, 34.0,
            "(?:mean\\s*corpuscular\\s*hemoglobin|mch)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*mch\\b",
            RANGE + NUM + "[ \\t]*mch\\b"),
        new PatternSet("MCHC", "g/dL", 32.0, 36.0,
            "(?:mchc|mean\\s*corpuscular\\s*hb\\s*conc)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*mchc\\b",
            RANGE + NUM + "[ \\t]*mchc\\b"),
        new PatternSet("RDW-CV", "%", 11.0, 16.0,
            "(?:rdw\\.?\\s*cv|rdw\\s+cv|rdw)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*rdw\\b",
            RANGE + NUM + "[ \\t]*rdw\\b"),
        new PatternSet("Total WBC Count", "/cumm", 4500.0, 11000.0,
            "(?:total\\s*wbc\\s*count|wbcs?\\s*count|wbc|white\\s*blood\\s*cells?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*(?:wbc|wbcs?)\\b",
            RANGE + NUM + "[ \\t]*(?:wbc|white\\s*blood)\\b"),
        new PatternSet("Neutrophils", "%", 35.0, 75.0,
            "(?:neutrophils?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*neutrophils?\\b",
            RANGE + NUM + "[ \\t]*neutrophils?\\b"),
        new PatternSet("Lymphocytes", "%", 24.0, 44.0,
            "(?:lymphocytes?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*lymphocytes?\\b",
            RANGE + NUM + "[ \\t]*lymphocytes?\\b"),
        new PatternSet("Monocytes", "%", 2.0, 12.0,
            "(?:monocytes?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*monocytes?\\b",
            RANGE + NUM + "[ \\t]*monocytes?\\b"),
        new PatternSet("Eosinophils", "%", 0.0, 6.0,
            "(?:eosinophils?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*eosinophils?\\b",
            RANGE + NUM + "[ \\t]*eosinophils?\\b"),
        new PatternSet("Basophils", "%", 0.0, 1.0,
            "(?:basophils?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*basophils?\\b",
            RANGE + NUM + "[ \\t]*basophils?\\b"),
        new PatternSet("Platelet Count", "Lakh/cumm", 1.5, 4.5,
            "(?:platelet\\s*count|platelet)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*platelet\\b",
            RANGE + NUM + "[ \\t]*platelet\\b"),
        new PatternSet("ESR", "mm/hr", 0.0, 22.0,
            "(?:corrected\\s*esr|esr)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*esr\\b",
            RANGE + NUM + "[ \\t]*esr\\b"),
        new PatternSet("Creatinine", "mg/dL", 0.5, 1.5,
            "(?:creatinine\\s*serum|creatinine)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*creatinine\\b",
            RANGE + NUM + "[ \\t]*creatinine\\b"),
        new PatternSet("Urine Pus Cells", "cells/HPF", 0.0, 5.0,
            "(?:urine\\s*pus\\s*cells?|pus\\s*cells?)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*pus\\s*cells?\\b",
            RANGE + NUM + "[ \\t]*pus\\s*cells?\\b"),
        new PatternSet("Urine Protein", "qualitative",
            "(?:urine\\s*protein|protein)\\s*[:.]?\\s*.*?" + QUAL,
            QUAL + "[ \\t]*protein\\b"),
        new PatternSet("Urine Sugar", "qualitative",
            "(?:urine\\s*sugar|sugar|glucose)\\s*[:.]?\\s*.*?" + QUAL,
            QUAL + "[ \\t]*(?:sugar|glucose)\\b"),
        new PatternSet("Urine RBC", "cells/HPF", 0.0, 2.0,
            "(?:red\\s*blood\\s*cells?|urine\\s*rbc|rbc)\\s*[:.]?\\s*.*?" + NUM,
            NUM + "[ \\t]*rbc\\b",
            RANGE + NUM + "[ \\t]*rbc\\b")
    );

    public ReportAnalysisResult analyze(byte[] fileBytes) throws Exception {
        String text;
        PDDocument doc = null;
        try {
            doc = Loader.loadPDF(fileBytes);
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
            log.info("PDFBox extracted {} chars", text.length());

            int totalPages = doc.getNumberOfPages();
            int charsPerPage = totalPages > 0 ? text.trim().length() / totalPages : 0;

            if (charsPerPage < 100) {
                log.info("Low text density ({} chars/page), falling back to OCR", charsPerPage);
                PDFRenderer renderer = new PDFRenderer(doc);
                StringBuilder ocrText = new StringBuilder();
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
                for (int i = 0; i < totalPages; i++) {
                    BufferedImage img = renderer.renderImageWithDPI(i, 300);
                    String pageText = tesseract.doOCR(img);
                    ocrText.append(pageText).append("\n");
                    log.info("OCR page {}: {} chars", i + 1, pageText.length());
                }
                text = ocrText.toString();
            }
        } finally {
            if (doc != null) doc.close();
        }

        return parseReport(text);
    }

    private ReportAnalysisResult parseReport(String text) {
        ReportAnalysisResult result = new ReportAnalysisResult();

        result.setVendor(detectVendor(text));

        PatientInfo patient = new PatientInfo();
        patient.setName(extractName(text));
        patient.setAge(extractAge(text));
        patient.setSex(extractSex(text));
        patient.setDate(extractDate(text));
        result.setPatient(patient);

        int parsed = 0;
        List<AlertItem> alerts = new ArrayList<>();

        for (PatternSet def : PARAMS) {
            ParameterResult pr = parseParameter(text, def);
            result.getParameters().add(pr);
            if (!"NOT_FOUND".equals(pr.getStatus())) {
                parsed++;
            }
            if ("HIGH".equals(pr.getStatus()) || "LOW".equals(pr.getStatus()) || "ABNORMAL".equals(pr.getStatus())) {
                AlertItem alert = new AlertItem();
                alert.setParameter(pr.getName());
                alert.setValue(pr.getValue());
                alert.setRange(pr.getRangeMin() != null ? pr.getRangeMin() + "\u2013" + pr.getRangeMax() : "N/A");
                boolean high = "HIGH".equals(pr.getStatus());
                alert.setDirection(high ? "Impossible" : "Too Low");
                String unit = pr.getUnit() != null ? " " + pr.getUnit() : "";
                alert.setMessage("\u26A0\uFE0F " + pr.getName() + " is " +
                    (high ? "Impossible / Too High" : "Very Low / Too Low") + ": " + pr.getValue() +
                    unit + " (expected " + (pr.getRangeMin() != null ? pr.getRangeMin() + "\u2013" + pr.getRangeMax() : "N/A") +
                    "). " + (high ? "Immediate medical attention recommended." : "Please consult a doctor."));
                alerts.add(alert);
            }
        }

        result.setParsedCount(parsed);
        result.setAlerts(alerts);

        if (parsed >= 16) result.setConfidence("High");
        else if (parsed >= 10) result.setConfidence("Medium");
        else result.setConfidence("Low");

        return result;
    }

    private String extractName(String text) {
        for (String regex : new String[]{
            "(?:patient\\s*name|patient|name)\\s*[:.]?\\s*([A-Za-z\\s.]+?)(?:\\s*(?:age|sex|gender|date|ref|sample))",
            "([A-Z][a-z]+\\s+[A-Z][a-z]+)"}) {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String n = m.group(1).trim();
                if (n.length() > 2) return n;
            }
        }
        return "";
    }

    private String extractAge(String text) {
        for (String regex : new String[]{
            "age\\s*[:.]?\\s*(\\d{1,3})",
            "(\\d{1,3})\\s*(?:yrs?|years?|yr)\\b"}) {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) return m.group(1);
        }
        return "";
    }

    private String extractSex(String text) {
        for (String regex : new String[]{
            "(?:sex|gender)\\s*[:.]?\\s*(Male|Female|M|F)",
            "\\b(Male|Female)\\b",
            "\\b(M|F)\\b(?!\\s*[:.])"}) {
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                String s = m.group(1).toUpperCase();
                if ("M".equals(s)) return "Male";
                if ("F".equals(s)) return "Female";
                return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            }
        }
        return "";
    }

    private String extractDate(String text) {
        Pattern p = Pattern.compile(
            "(?:date|report\\s*date|collected|collection\\s*date)\\s*[:.]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String detectVendor(String text) {
        String upper = text.toUpperCase();
        if (upper.contains("SHIVANI")) return "Shivani Diagnostic Centre";
        if (upper.contains("STAR LAB") || upper.contains("STARLAB705")) return "Star Lab";
        return "Unknown";
    }

    private ParameterResult parseParameter(String text, PatternSet def) {
        ParameterResult pr = new ParameterResult();
        pr.setName(def.name);
        pr.setUnit(def.unit);
        pr.setRangeMin(def.min);
        pr.setRangeMax(def.max);

        String matchedValue = null;
        for (Pattern p : def.patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                matchedValue = m.group(1).trim();
                break;
            }
        }

        if (matchedValue == null || matchedValue.isEmpty()) {
            pr.setStatus("NOT_FOUND");
            pr.setValue("");
            return pr;
        }

        String cleaned = cleanValue(matchedValue);
        pr.setRawText(matchedValue);

        if (def.qualitative) {
            String lower = cleaned.toLowerCase();
            if ("nil".equals(lower) || "absent".equals(lower) || "negative".equals(lower) || "normal".equals(lower)) {
                pr.setValue(cleaned);
                pr.setStatus("NORMAL");
            } else {
                pr.setValue(cleaned);
                pr.setStatus("ABNORMAL");
            }
            return pr;
        }

        try {
            String numeric = cleaned.replaceAll(",", "");
            double val = Double.parseDouble(numeric);
            pr.setValue(cleaned);

            if (val < def.min) {
                pr.setStatus("LOW");
            } else if (val > def.max) {
                pr.setStatus("HIGH");
            } else {
                pr.setStatus("NORMAL");
            }
        } catch (NumberFormatException e) {
            pr.setValue(cleaned);
            pr.setStatus("NOT_FOUND");
        }

        return pr;
    }

    private String cleanValue(String v) {
        if (v == null || v.isBlank()) return v;
        String cleaned = v.replaceAll("[^\\d.,A-Za-z+\\-].*$", "").trim();
        return cleaned.isBlank() ? v.split("\\s+")[0] : cleaned;
    }
}
