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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportAnalyzerService {

    private static final Logger log = LoggerFactory.getLogger(ReportAnalyzerService.class);

    private static final String NUM = "(\\d+(?:[.,]\\d+)?)";
    private static final String NOT_RANGE = "(?!\\s*[-\\u2013]\\s*\\d)";

    private static final Map<String, ParamInfo> PARAMS = new LinkedHashMap<>();

    private static class ParamInfo {
        final String displayName;
        final String unit;
        final Double min;
        final Double max;
        final boolean qualitative;
        final String nameRegex;

        ParamInfo(String displayName, String unit, Double min, Double max, String nameRegex) {
            this.displayName = displayName;
            this.unit = unit;
            this.min = min;
            this.max = max;
            this.qualitative = false;
            this.nameRegex = nameRegex;
        }

        ParamInfo(String displayName, String unit, String nameRegex) {
            this.displayName = displayName;
            this.unit = unit;
            this.min = null;
            this.max = null;
            this.qualitative = true;
            this.nameRegex = nameRegex;
        }
    }

    static {
        PARAMS.put("haemoglobin", new ParamInfo("Haemoglobin", "g/dL", 13.5, 17.5,
            "h[ae]moglobin(?:\\s*\\([^)]*\\))?"));
        PARAMS.put("rbcCount", new ParamInfo("RBC Count", "milli./cu.mm", 4.5, 5.9,
            "rbc[s]?\\s*count|red\\s*blood\\s*cells?"));
        PARAMS.put("pcv", new ParamInfo("PCV / HCT", "%", 37.0, 53.0,
            "packed\\s*cell\\s*volume|pcv|hct(?:\\s*\\([^)]*\\))?"));
        PARAMS.put("mcv", new ParamInfo("MCV", "fL", 80.0, 100.0,
            "mean\\s*corpuscular\\s*volume|\\bmcv\\b(?:\\s*\\([^)]*\\))?"));
        PARAMS.put("mch", new ParamInfo("MCH", "pg", 26.0, 34.0,
            "mean\\s*corpuscular\\s*h[ae]moglobin(?!\\s*conc)(?:\\s*\\([^)]*\\))?"));
        PARAMS.put("mchc", new ParamInfo("MCHC", "g/dL", 32.0, 36.0,
            "mchc|mean\\s*corpuscular\\s*hb\\s*conc(?:\\s*\\([^)]*\\))?"));
        PARAMS.put("rdw", new ParamInfo("RDW-CV", "%", 11.0, 16.0,
            "rdw[\\s-]cv"));
        PARAMS.put("wbcCount", new ParamInfo("Total WBC Count", "/cumm", 4500.0, 11000.0,
            "total\\s*wbc[s]?\\s*count"));
        PARAMS.put("neutrophils", new ParamInfo("Neutrophils", "%", 35.0, 75.0,
            "neutrophils"));
        PARAMS.put("lymphocytes", new ParamInfo("Lymphocytes", "%", 24.0, 44.0,
            "lymphocytes"));
        PARAMS.put("monocytes", new ParamInfo("Monocytes", "%", 2.0, 12.0,
            "monocytes"));
        PARAMS.put("eosinophils", new ParamInfo("Eosinophils", "%", 0.0, 6.0,
            "eosinophils"));
        PARAMS.put("basophils", new ParamInfo("Basophils", "%", 0.0, 1.0,
            "basophils"));
        PARAMS.put("plateletCount", new ParamInfo("Platelet Count", "Lakh/cumm", 1.5, 4.5,
            "platelet\\s*count"));
        PARAMS.put("esr", new ParamInfo("ESR", "mm/hr", 0.0, 22.0,
            "(?:corrected\\s*)?esr\\b"));
        PARAMS.put("creatinine", new ParamInfo("Creatinine", "mg/dL", 0.5, 1.5,
            "creatinine(?:\\s*serum)?"));
        PARAMS.put("pusCells", new ParamInfo("Urine Pus Cells", "cells/HPF", 0.0, 5.0,
            "pus\\s*cells?"));
        PARAMS.put("urineProtein", new ParamInfo("Urine Protein", "qualitative",
            "protein"));
        PARAMS.put("urineSugar", new ParamInfo("Urine Sugar", "mg/dL", 0.0, 139.0,
            "rbs|sugar|glucose"));
        PARAMS.put("urineRbc", new ParamInfo("Urine RBC", "cells/HPF", 0.0, 2.0,
            "red\\s*blood\\s*cells?|urine\\s*rbc"));
    }

    public ReportAnalysisResult analyze(byte[] fileBytes) throws Exception {
        String text = extractTextRaw(fileBytes);
        return parseReport(text, detectVendor(text));
    }

    public String extractTextOnly(byte[] fileBytes) throws Exception {
        return extractTextRaw(fileBytes);
    }

    private String extractTextRaw(byte[] fileBytes) throws Exception {
        String text;
        PDDocument doc = null;
        try {
            doc = Loader.loadPDF(fileBytes);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
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
        return text;
    }

    private ReportAnalysisResult parseReport(String text, String vendor) {
        ReportAnalysisResult result = new ReportAnalysisResult();
        result.setVendor(vendor);

        PatientInfo patient = new PatientInfo();
        patient.setName(extractName(text));
        patient.setAge(extractAge(text));
        patient.setSex(extractSex(text));
        patient.setDate(extractDate(text));
        result.setPatient(patient);

        int parsed = 0;
        List<AlertItem> alerts = new ArrayList<>();

        for (Map.Entry<String, ParamInfo> e : PARAMS.entrySet()) {
            String key = e.getKey();
            ParamInfo info = e.getValue();
            boolean isShivani = "Shivani Diagnostic Centre".equals(vendor);

            ParameterResult pr;
            if ("urineSugar".equals(key) && isShivani) {
                pr = parseNumericParam(text, info);
            } else if ("urineProtein".equals(key)) {
                pr = parseQualitativeParam(text, info);
            } else if ("urineSugar".equals(key)) {
                pr = parseQualitativeParam(text, info);
            } else if ("pusCells".equals(key)) {
                pr = parsePusCells(text, info);
            } else {
                pr = parseNumericParam(text, info);
            }

            result.getParameters().add(pr);
            if (!"NOT_FOUND".equals(pr.getStatus())) parsed++;

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
        result.setConfidence(parsed >= 16 ? "High" : parsed >= 10 ? "Medium" : "Low");
        return result;
    }

    private ParameterResult parseNumericParam(String text, ParamInfo info) {
        ParameterResult pr = new ParameterResult();
        pr.setName(info.displayName);
        pr.setUnit(info.unit);
        pr.setRangeMin(info.min);
        pr.setRangeMax(info.max);

        String val = null;

        if (info.nameRegex.contains("esr")) {
            val = extractEsr(text);
            if (val != null) {
                trySetNumeric(pr, val);
                if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
            }
        }

        val = extractFirstValue(text, info.nameRegex);
        if (val != null) {
            trySetNumeric(pr, val);
            if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
        }

        val = extractValueBeforeName(text, info.nameRegex);
        if (val != null) {
            trySetNumeric(pr, val);
            if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
        }

        pr.setValue("");
        pr.setStatus("NOT_FOUND");
        return pr;
    }

    private ParameterResult parsePusCells(String text, ParamInfo info) {
        ParameterResult pr = new ParameterResult();
        pr.setName(info.displayName);
        pr.setUnit(info.unit);
        pr.setRangeMin(info.min);
        pr.setRangeMax(info.max);

        Pattern p = Pattern.compile(
            "(?i)pus\\s*cells?[^\\d\\n]{0,20}?(\\d+)\\s*[-\\u2013]",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            trySetNumeric(pr, m.group(1));
            if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
        }

        String val = extractFirstValue(text, info.nameRegex);
        if (val != null) {
            trySetNumeric(pr, val);
            if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
        }

        val = extractValueBeforeName(text, info.nameRegex);
        if (val != null) {
            trySetNumeric(pr, val);
            if (!"NOT_FOUND".equals(pr.getStatus())) return pr;
        }

        pr.setValue("");
        pr.setStatus("NOT_FOUND");
        return pr;
    }

    private ParameterResult parseQualitativeParam(String text, ParamInfo info) {
        ParameterResult pr = new ParameterResult();
        pr.setName(info.displayName);
        pr.setUnit(info.unit);
        pr.setRangeMin(info.min);
        pr.setRangeMax(info.max);

        Pattern p = Pattern.compile(
            "(?i)" + info.nameRegex + "[^\\n]{0,20}?\\b(Nil|Absent|Negative|Normal|Present|\\+{1,4})\\b");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String raw = m.group(1);
            pr.setRawText(raw);
            String lower = raw.toLowerCase();
            if ("nil".equals(lower) || "absent".equals(lower) || "negative".equals(lower) || "normal".equals(lower)) {
                pr.setValue(raw);
                pr.setStatus("NORMAL");
            } else {
                pr.setValue(raw);
                pr.setStatus("ABNORMAL");
            }
            return pr;
        }

        pr.setValue("");
        pr.setStatus("NOT_FOUND");
        return pr;
    }

    private String extractEsr(String text) {
        Pattern p = Pattern.compile(
            "(?i)(?:corrected\\s*)?esr[^\\d\\n]*?\\n\\s*(\\d+\\.?\\d*)",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    private String extractFirstValue(String text, String nameRegex) {
        Pattern p = Pattern.compile(
            "(?i)" + nameRegex + "[^\\d\\n]{0,40}?" + NUM + NOT_RANGE,
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).replace(",", "");
        return null;
    }

    private String extractValueBeforeName(String text, String nameRegex) {
        Pattern p = Pattern.compile(
            "(?i)" + NUM + "[ \\t]+" + nameRegex + "\\b",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).replace(",", "");
        return null;
    }

    private void trySetNumeric(ParameterResult pr, String raw) {
        try {
            String cleaned = raw.replace(",", "");
            double val = Double.parseDouble(cleaned);
            pr.setValue(cleaned);
            pr.setRawText(raw);
            if (val < pr.getRangeMin()) pr.setStatus("LOW");
            else if (val > pr.getRangeMax()) pr.setStatus("HIGH");
            else pr.setStatus("NORMAL");
        } catch (NumberFormatException e) {
            pr.setValue(raw);
            pr.setRawText(raw);
            pr.setStatus("NOT_FOUND");
        }
    }

    private String extractName(String text) {
        Pattern p = Pattern.compile(
            "(?i)\\bname\\s*[:.]?\\s*((?:Mr|Mrs|Ms|Dr)\\.?\\s+[A-Za-z\\s.]+?)(?=\\s{2,}|\\n|Sample|Sex|Ref|/|$)",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String n = m.group(1).trim();
            if (n.length() > 2) return n;
        }

        p = Pattern.compile(
            "(?i)(?:patient\\s*name|patient|name)\\s*[:.]?\\s*([A-Za-z\\s.]+?)(?=\\s{2,}|\\n|Sex|Age|Ref|Sample|$)",
            Pattern.MULTILINE);
        m = p.matcher(text);
        if (m.find()) {
            String n = m.group(1).trim();
            if (n.length() > 2) return n;
        }

        p = Pattern.compile("([A-Z][a-z]+\\s+[A-Z][a-z]+(?:\\s+[A-Z][a-z]+)?)");
        m = p.matcher(text);
        if (m.find()) return m.group(1).trim();

        return "";
    }

    private String extractAge(String text) {
        Pattern p = Pattern.compile(
            "(?i)(?:sex/age|age)\\s*[:.]?\\s*(?:male|female)?\\s*/\\s*(\\d{1,3})\\s*(?:years?|yrs?|yr)",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1);

        p = Pattern.compile(
            "(?i)age\\s*[:.]?\\s*(\\d{1,3})\\s*(?:years?|yrs?|yr)",
            Pattern.MULTILINE);
        m = p.matcher(text);
        if (m.find()) return m.group(1);

        p = Pattern.compile(
            "(?i)(\\d{1,3})\\s*(?:years?|yrs?|yr)\\b");
        m = p.matcher(text);
        if (m.find()) return m.group(1);

        return "";
    }

    private String extractSex(String text) {
        Pattern p = Pattern.compile(
            "(?i)(?:sex|gender)\\s*[:.]?\\s*(Male|Female|M|F)",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            String s = m.group(1).toUpperCase();
            if ("M".equals(s)) return "Male";
            if ("F".equals(s)) return "Female";
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }

        p = Pattern.compile("\\b(Male|Female)\\b");
        m = p.matcher(text);
        if (m.find()) return m.group(1);

        return "";
    }

    private String extractDate(String text) {
        Pattern p = Pattern.compile(
            "(?i)(?:sample\\s*collected\\s*on|date|collection\\s*date|report\\s*date)\\s*[:.]?\\s*(\\d{1,2}[-/][A-Za-z]{3}[-/]\\d{2,4}|\\d{1,2}[-/]\\d{1,2}[-/]\\d{2,4})",
            Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "";
    }

    private String detectVendor(String text) {
        String upper = text.toUpperCase();
        if (upper.contains("SHIVANI")) return "Shivani Diagnostic Centre";
        if (upper.contains("STAR LAB") || upper.contains("STARLAB705")) return "Star Lab";
        return "Unknown";
    }
}
