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

    private static class ParamDef {
        final String name;
        final double min;
        final double max;
        final String unit;
        final boolean qualitative;
        final List<String> aliases;

        ParamDef(String name, double min, double max, String unit, String... aliases) {
            this.name = name;
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.qualitative = false;
            this.aliases = List.of(aliases);
        }

        ParamDef(String name, String unit, String... aliases) {
            this.name = name;
            this.min = 0;
            this.max = 0;
            this.unit = unit;
            this.qualitative = true;
            this.aliases = List.of(aliases);
        }
    }

    private static List<ParamDef> PARAM_DEFS;

    private static synchronized List<ParamDef> getParamDefs() {
        if (PARAM_DEFS == null) {
            PARAM_DEFS = List.of(
                new ParamDef("Haemoglobin", 13.5, 17.5, "g/dL",
                    "haemoglobin", "hemoglobin"),
                new ParamDef("RBC Count", 4.5, 5.9, "milli./cu.mm",
                    "rbc count", "rbcs count", "rbc's count"),
                new ParamDef("PCV / HCT", 37.0, 53.0, "%",
                    "packed cell volume", "pcv", "hct"),
                new ParamDef("MCV", 80.0, 100.0, "fL",
                    "mean corpuscular volume", "mcv"),
                new ParamDef("MCH", 26.0, 34.0, "pg",
                    "mean corpuscular hemoglobin", "mean corpuscular haemoglobin", " mch "),
                new ParamDef("MCHC", 32.0, 36.0, "g/dL",
                    "mchc", "mean corpuscular hb conc", "mean corpuscular hemoglobin concentration"),
                new ParamDef("RDW-CV", 11.0, 16.0, "%",
                    "rdw-cv", "rdw cv", "rdw_cv"),
                new ParamDef("Total WBC Count", 4500, 11000, "/cumm",
                    "total wbc count", "total wbcs count", "total wbc's count", "total leucocyte"),
                new ParamDef("Neutrophils", 35.0, 75.0, "%",
                    "neutrophils"),
                new ParamDef("Lymphocytes", 24.0, 44.0, "%",
                    "lymphocytes"),
                new ParamDef("Monocytes", 2.0, 12.0, "%",
                    "monocytes"),
                new ParamDef("Eosinophils", 0.0, 6.0, "%",
                    "eosinophils"),
                new ParamDef("Basophils", 0.0, 1.0, "%",
                    "basophils"),
                new ParamDef("Platelet Count", 1.5, 4.5, "Lakh/cumm",
                    "platelet count", "platelets count"),
                new ParamDef("ESR", 0.0, 15.0, "mm/hr",
                    "corrected esr", "esr"),
                new ParamDef("Creatinine", 0.5, 1.5, "mg/dL",
                    "creatinine serum", "creatinine"),
                new ParamDef("Urine Pus Cells", 0.0, 5.0, "cells/HPF",
                    "pus cells"),
                new ParamDef("Urine Protein", "qualitative",
                    "protein"),
                new ParamDef("Urine Sugar", "qualitative",
                    "sugar", "glucose", "rbs"),
                new ParamDef("Urine RBC", 0.0, 2.0, "cells/HPF",
                    "red blood cells")
            );
        }
        return PARAM_DEFS;
    }

    public ReportAnalysisResult analyze(byte[] fileBytes) throws Exception {
        String text = extractTextRaw(fileBytes);
        String vendor = detectVendor(text);
        return parseReport(text, vendor);
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

        result.setPatient(extractPatientInfo(text));

        String[] lines = text.split("\\n");
        List<AlertItem> alerts = new ArrayList<>();
        int parsed = 0;

        for (ParamDef def : getParamDefs()) {
            ParameterResult pr = findParameter(lines, def, vendor);
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

    private ParameterResult findParameter(String[] lines, ParamDef def, String vendor) {
        for (int i = 0; i < lines.length; i++) {
            String lineLower = lines[i].toLowerCase().trim();
            boolean matches = false;
            for (String alias : def.aliases) {
                if (lineLower.contains(alias.toLowerCase())) {
                    matches = true;
                    break;
                }
            }
            if (!matches) continue;

            if (def.qualitative) {
                return extractQualitative(lines, i, def);
            }

            if ("Urine Pus Cells".equals(def.name)) {
                Pattern rangeResult = Pattern.compile("(\\d+)\\s*[-\\u2013]\\s*(\\d+)\\s*cells", Pattern.CASE_INSENSITIVE);
                Matcher rm = rangeResult.matcher(lines[i]);
                if (rm.find()) {
                    return buildResult(def, Double.parseDouble(rm.group(1)));
                }
            }

            if ("ESR".equals(def.name) && i + 1 < lines.length) {
                String combined = lines[i] + " " + lines[i + 1];
                Double val = extractResultValue(combined);
                if (val != null) return buildResult(def, val);
            }

            String searchText = lines[i];
            if (i + 1 < lines.length) searchText += " " + lines[i + 1];
            if (i + 2 < lines.length) searchText += " " + lines[i + 2];

            Double val = extractResultValue(searchText);
            if (val != null) return buildResult(def, val);
        }

        return notFound(def);
    }

    private Double extractResultValue(String text) {
        String cleaned = text.replaceAll("(\\d+\\.?\\d*)\\s*[-\\u2013]\\s*(\\d+\\.?\\d*)", " RANGE ");
        Pattern p = Pattern.compile("(?<![\\d.])\\b(\\d+(?:[.,]\\d+)?)\\b(?!\\s*[-\\u2013]\\s*\\d)");
        Matcher m = p.matcher(cleaned);
        while (m.find()) {
            String numStr = m.group(1).replace(",", "");
            try {
                double val = Double.parseDouble(numStr);
                if (numStr.replace(".", "").length() <= 6) {
                    return val;
                }
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private ParameterResult extractQualitative(String[] lines, int lineIndex, ParamDef def) {
        Pattern p = Pattern.compile("(?i)(nil|absent|negative|normal|present|\\++)");
        Matcher m = p.matcher(lines[lineIndex]);
        if (m.find()) {
            String val = m.group(1).toLowerCase();
            boolean isNormal = val.matches("nil|absent|negative|normal");
            ParameterResult r = new ParameterResult();
            r.setName(def.name);
            r.setValue(val);
            r.setUnit("qualitative");
            r.setStatus(isNormal ? "NORMAL" : "ABNORMAL");
            return r;
        }
        return notFound(def);
    }

    private ParameterResult buildResult(ParamDef def, double val) {
        ParameterResult pr = new ParameterResult();
        pr.setName(def.name);
        pr.setUnit(def.unit);
        pr.setRangeMin(def.min);
        pr.setRangeMax(def.max);
        pr.setValue(String.valueOf(val));
        if (val < def.min) pr.setStatus("LOW");
        else if (val > def.max) pr.setStatus("HIGH");
        else pr.setStatus("NORMAL");
        return pr;
    }

    private ParameterResult notFound(ParamDef def) {
        ParameterResult pr = new ParameterResult();
        pr.setName(def.name);
        pr.setUnit(def.unit);
        pr.setRangeMin(def.qualitative ? null : def.min);
        pr.setRangeMax(def.qualitative ? null : def.max);
        pr.setValue("");
        pr.setStatus("NOT_FOUND");
        return pr;
    }

    private PatientInfo extractPatientInfo(String text) {
        PatientInfo info = new PatientInfo();

        Pattern nameP = Pattern.compile(
            "(?i)(?:^|name\\s*[:.]?\\s*)((?:Mr|Mrs|Ms|Dr|MR|MRS|MS)\\.?\\s+[A-Z][a-zA-Z\\s]{2,30}?)(?=\\s{2,}|\\n|$|Sample|Sex|Ref)",
            Pattern.MULTILINE);
        Matcher nm = nameP.matcher(text);
        if (nm.find()) info.setName(nm.group(1).trim());

        if (info.getName().isEmpty()) {
            Pattern nameP2 = Pattern.compile(
                "(?i)(?:patient\\s*name|patient|name)\\s*[:.]?\\s*([A-Z][a-zA-Z\\s.]+?)(?=\\s{2,}|\\n|Sex|Age|Ref|Sample|$)",
                Pattern.MULTILINE);
            Matcher nm2 = nameP2.matcher(text);
            if (nm2.find()) {
                String n = nm2.group(1).trim();
                if (n.length() > 2) info.setName(n);
            }
        }

        Pattern ageP = Pattern.compile(
            "(?i)(?:sex/age|age)\\s*[:.]?\\s*(?:male|female|m|f)?\\s*/\\s*(\\d{1,3})\\s*years?");
        Matcher am = ageP.matcher(text);
        if (am.find()) info.setAge(am.group(1));

        if (info.getAge().isEmpty()) {
            Pattern ageP2 = Pattern.compile("(\\d{1,3})\\s*years?\\s*/[MF]", Pattern.CASE_INSENSITIVE);
            Matcher am2 = ageP2.matcher(text);
            if (am2.find()) info.setAge(am2.group(1));
        }

        if (info.getAge().isEmpty()) {
            Pattern ageP3 = Pattern.compile(
                "(?i)age\\s*[:.]?\\s*(\\d{1,3})\\s*(?:years?|yrs?|yr)\\b");
            Matcher am3 = ageP3.matcher(text);
            if (am3.find()) info.setAge(am3.group(1));
        }

        Pattern sexP = Pattern.compile(
            "(?i)(?:sex|gender)\\s*[:.]?\\s*(Male|Female|M|F)");
        Matcher sm = sexP.matcher(text);
        if (sm.find()) {
            String s = sm.group(1).toUpperCase();
            info.setSex("M".equals(s) ? "Male" : "F".equals(s) ? "Female" :
                s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
        }

        if (info.getSex().isEmpty()) {
            Pattern sexP2 = Pattern.compile("\\b(Male|Female)\\b");
            Matcher sm2 = sexP2.matcher(text);
            if (sm2.find()) info.setSex(sm2.group(1));
        }

        Pattern dateP = Pattern.compile(
            "(?i)(?:sample\\s*collected\\s*on|collected\\s*at|received|collection\\s*date|report\\s*date)\\s*[:.\\s]+(\\d{2}[/-][A-Za-z0-9]{2,3}[/-]\\d{4})");
        Matcher dm = dateP.matcher(text);
        if (dm.find()) info.setDate(dm.group(1));

        return info;
    }

    private String detectVendor(String text) {
        String t = text.toLowerCase();
        if (t.contains("shivani") || t.contains("bhawanipur")) return "Shivani Diagnostic Centre";
        if (t.contains("starlab") || t.contains("star lab") ||
            t.contains("chatrchaya") || t.contains("pithampur") ||
            t.contains("dharambeer") || t.contains("patidar kirana"))
            return "Star Lab";
        return "Unknown";
    }
}
