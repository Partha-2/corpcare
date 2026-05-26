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

    private static class ParamDef {
        final String name;
        final String regex;
        final String unit;
        final Double min;
        final Double max;
        final boolean qualitative;

        ParamDef(String name, String regex, String unit, Double min, Double max) {
            this.name = name;
            this.regex = regex;
            this.unit = unit;
            this.min = min;
            this.max = max;
            this.qualitative = false;
        }

        ParamDef(String name, String regex, String unit) {
            this.name = name;
            this.regex = regex;
            this.unit = unit;
            this.min = null;
            this.max = null;
            this.qualitative = true;
        }
    }

    private static final List<ParamDef> PARAMS = List.of(
        new ParamDef("Haemoglobin",
            "(?:haemoglobin|hemoglobin|hb)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "g/dL", 13.5, 17.5),
        new ParamDef("RBC Count",
            "(?:rbc\\s*count|rbcs\\s*count|red\\s*blood\\s*cells?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "milli./cu.mm", 4.5, 5.9),
        new ParamDef("PCV / HCT",
            "(?:packed\\s*cell\\s*volume|pcv|hct)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 37.0, 53.0),
        new ParamDef("MCV",
            "(?:mean\\s*corpuscular\\s*volume|mcv)\\b\\s*[:.]?\\s*(\\d+\\.?\\d*)", "fL", 80.0, 100.0),
        new ParamDef("MCH",
            "(?:mean\\s*corpuscular\\s*hemoglobin|mch)\\b\\s*[:.]?\\s*(\\d+\\.?\\d*)", "pg", 26.0, 34.0),
        new ParamDef("MCHC",
            "(?:mchc|mean\\s*corpuscular\\s*hb\\s*conc)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "g/dL", 32.0, 36.0),
        new ParamDef("RDW-CV",
            "(?:rdw\\.?\\s*cv|rdw\\s+cv|rdw)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 11.0, 16.0),
        new ParamDef("Total WBC Count",
            "(?:total\\s*wbc\\s*count|total\\s*wbcs?\\s*count|wbc\\s*count|white\\s*blood\\s*cells?)\\s*[:.]?\\s*(\\d[\\d,\\.]*)", "/cumm", 4500.0, 11000.0),
        new ParamDef("Neutrophils",
            "(?:neutrophils?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 35.0, 75.0),
        new ParamDef("Lymphocytes",
            "(?:lymphocytes?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 24.0, 44.0),
        new ParamDef("Monocytes",
            "(?:monocytes?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 2.0, 12.0),
        new ParamDef("Eosinophils",
            "(?:eosinophils?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 0.0, 6.0),
        new ParamDef("Basophils",
            "(?:basophils?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "%", 0.0, 1.0),
        new ParamDef("Platelet Count",
            "(?:platelet\\s*count)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "Lakh/cumm", 1.5, 4.5),
        new ParamDef("ESR",
            "(?:corrected\\s*esr|esr)\\b\\s*[:.]?\\s*(\\d+\\.?\\d*)", "mm/hr", 0.0, 15.0),
        new ParamDef("Creatinine",
            "(?:creatinine\\s*serum|creatinine)\\b\\s*[:.]?\\s*(\\d+\\.?\\d*)", "mg/dL", 0.5, 1.5),
        new ParamDef("Urine Pus Cells",
            "(?:pus\\s*cells?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "cells/HPF", 0.0, 5.0),
        new ParamDef("Urine Protein",
            "(?:protein)\\s*[:.]?\\s*(\\w+)", "qualitative"),
        new ParamDef("Urine Sugar",
            "(?:sugar|glucose)\\s*[:.]?\\s*(\\w+)", "qualitative"),
        new ParamDef("Urine RBC",
            "(?:red\\s*blood\\s*cells?|rbc)\\s*[:.]?\\s*(\\d+\\.?\\d*)", "cells/HPF", 0.0, 2.0)
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
        patient.setName(extractGroup(text, "(?:name|patient\\s*name|patient)\\s*[:.]?\\s*([A-Za-z\\s.]+)", 1));
        patient.setAge(extractGroup(text, "(?:age)\\s*[:.]?\\s*(\\d{1,3})", 1));
        patient.setSex(extractGroup(text, "(?:sex|gender)\\s*[:.]?\\s*(male|female|m|f)", 1));
        patient.setDate(extractGroup(text,
            "(?:date|report\\s*date|collected|collection\\s*date)\\s*[:.]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", 1));
        result.setPatient(patient);

        int parsed = 0;
        List<AlertItem> alerts = new ArrayList<>();

        for (ParamDef def : PARAMS) {
            ParameterResult pr = parseParameter(text, def);
            result.getParameters().add(pr);
            if (!"NOT_FOUND".equals(pr.getStatus())) {
                parsed++;
            }
            if ("HIGH".equals(pr.getStatus()) || "LOW".equals(pr.getStatus()) || "ABNORMAL".equals(pr.getStatus())) {
                AlertItem alert = new AlertItem();
                alert.setParameter(pr.getName());
                alert.setValue(pr.getValue());
                alert.setRange(pr.getRangeMin() != null ? pr.getRangeMin() + "–" + pr.getRangeMax() : "N/A");
                String dir = "HIGH".equals(pr.getStatus()) ? "High" : "LOW".equals(pr.getStatus()) ? "Low" : "Abnormal";
                alert.setDirection(dir);
                String unit = pr.getUnit() != null ? " " + pr.getUnit() : "";
                alert.setMessage("⚠️ " + pr.getName() + " is " + dir + ": " + pr.getValue() +
                    unit + " (expected " + (pr.getRangeMin() != null ? pr.getRangeMin() + "–" + pr.getRangeMax() : "N/A") +
                    "). Please consult a doctor.");
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

    private String detectVendor(String text) {
        String upper = text.toUpperCase();
        if (upper.contains("SHIVANI")) return "Shivani Diagnostic Centre";
        if (upper.contains("STAR LAB") || upper.contains("STARLAB705")) return "Star Lab";
        return "Unknown";
    }

    private ParameterResult parseParameter(String text, ParamDef def) {
        ParameterResult pr = new ParameterResult();
        pr.setName(def.name);
        pr.setUnit(def.unit);
        pr.setRangeMin(def.min);
        pr.setRangeMax(def.max);

        Pattern p = Pattern.compile(def.regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(text);

        if (!m.find()) {
            pr.setStatus("NOT_FOUND");
            pr.setValue("");
            return pr;
        }

        String raw = m.group(1).trim();
        pr.setRawText(raw);

        if (def.qualitative) {
            String lower = raw.toLowerCase();
            if ("nil".equals(lower) || "absent".equals(lower) || "negative".equals(lower)) {
                pr.setValue(raw);
                pr.setStatus("NORMAL");
            } else {
                pr.setValue(raw);
                pr.setStatus("ABNORMAL");
            }
            return pr;
        }

        try {
            String cleaned = raw.replaceAll(",", "");
            double val = Double.parseDouble(cleaned);
            pr.setValue(cleaned);

            if (val < def.min) {
                pr.setStatus("LOW");
            } else if (val > def.max) {
                pr.setStatus("HIGH");
            } else {
                pr.setStatus("NORMAL");
            }
        } catch (NumberFormatException e) {
            pr.setValue(raw);
            pr.setStatus("NOT_FOUND");
        }

        return pr;
    }

    private String extractGroup(String text, String regex, int group) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(group).trim() : "";
    }
}
