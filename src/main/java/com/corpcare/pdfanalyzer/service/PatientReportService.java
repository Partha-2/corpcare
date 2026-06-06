package com.corpcare.pdfanalyzer.service;

import com.corpcare.entity.AnalyzedPatient;
import com.corpcare.entity.AnalyzedReport;
import com.corpcare.pdfanalyzer.model.response.PatientReport;
import com.corpcare.pdfanalyzer.model.response.ReportParameter;
import com.corpcare.pdfsplit.service.image.ImageExtractorService;
import com.corpcare.pdfsplit.service.image.ImageValueExtractor;
import com.corpcare.repository.AnalyzedPatientRepository;
import com.corpcare.repository.AnalyzedReportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientReportService {

    private static final String BASE_URL = "/api/patient";

    private static final String[] MEDICAL_KEYWORDS = {
        "patient","report","blood","ecg","urine","haemoglobin",
        "hemoglobin","glucose","tsh","wbc","rbc","platelet","tlc",
        "leucocyte","qrs","bpm","diagnosis","lab","test","hospital",
        "clinic","doctor","creatinine","cholesterol","thyroid","complete"
    };

    private static final Set<String> SKIP_FIELDS =
            Set.of("Patient Name","Patient ID","Age","Date");

    private static final String[] ID_PATTERNS = {
        "patient\\s+id\\s*[:\\-]?\\s*([0-9]{4,12})",
        "patient\\s+no\\s*[:\\-]?\\s*([0-9]{4,12})",
        "ref\\.?\\s*no\\s*[:\\-]?\\s*([0-9]{4,12})",
        "lab\\s+no\\s*[:\\-]?\\s*([0-9]{4,12})",
        "sample\\s+no\\s*[:\\-]?\\s*([0-9]{4,12})",
        "uhid\\s*[:\\-]?\\s*([0-9]{4,12})",
    };

    private static final String[] NAME_PATTERNS = {
        "patient\\s+name\\s*[:\\-]\\s*"
            + "(?:mr\\.?\\s*|mrs\\.?\\s*|ms\\.?\\s*|dr\\.?\\s*)?"
            + "([a-z][a-z\\s]{2,35}?)"
            + "(?:\\s+[0-9]|\\s+lab|\\s+ref|\\s+age|\\n|$)",
        "\\bname\\s*[:\\-]\\s*"
            + "(?:mr\\.?\\s*|mrs\\.?\\s*|ms\\.?\\s*|dr\\.?\\s*)?"
            + "([a-z][a-z\\s]{2,35}?)"
            + "(?:\\s+[0-9]|\\s+lab|\\s+ref|\\s+age|\\n|$)",
    };

    private static final String[] GENDER_PATTERNS = {
        "\\bsex\\s*[:\\-]?\\s*(male|female)\\b",
        "\\bgender\\s*[:\\-]?\\s*(male|female)\\b",
        "age\\s*/\\s*sex\\s*[:\\-]?\\s*[0-9]+\\s*[/\\s]*(male|female)\\b",
        "[0-9]+\\s*(?:yrs?|years?)\\s*/\\s*(male|female)\\b",
        "\\|\\s*(male|female)\\b",
        "\\(\\s*(male|female)\\s*\\)",
    };

    private static final String[] AGE_PATTERNS = {
        "\\bage\\s*[:\\-]?\\s*([0-9]{1,3})\\s*(?:years?|yrs?)\\b",
        "\\bage\\s*/\\s*sex\\s*[:\\-]?\\s*([0-9]{1,3})",
        "([0-9]{1,3})\\s*(?:years?|yrs?)\\s*/\\s*(?:male|female)\\b",
        "([0-9]{2})\\s*\\|\\s*(?:male|female)",
        "\\bage\\s*[:\\-]\\s*([0-9]{1,3})\\b",
    };

    private final ReportExtractorService extractor;
    private final ReferenceRangeService referenceRange;
    private final ImageExtractorService imageExtractor;
    private final ImageValueExtractor imageValueExtractor;
    private final AnalyzedPatientRepository patientRepo;
    private final AnalyzedReportRepository reportRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public PatientReport analyze(PDDocument doc, String patientId,
                                  String name, String gender, int age) throws IOException {
        String[] pages = readAllPages(doc);
        String text = pages[0];
        String srcType = pages[1];

        validateMedicalContent(text);
        validatePatientDetails(text, patientId, name, gender, age);

        String reportType = extractor.detectType(text);
        boolean hasBlood = text.contains("haemoglobin") || text.contains("hemoglobin");
        boolean hasUrine = text.contains("specific gravity") || text.contains("microscopy");
        boolean hasChest = text.contains("lung fields") || text.contains("trachea");
        boolean hasEye = text.contains("eye vision") || text.contains("color vision");
        boolean isECG = "ECG".equals(reportType);
        int typeCount = (hasBlood ? 1 : 0) + (hasUrine ? 1 : 0) + (hasChest ? 1 : 0) + (hasEye ? 1 : 0);
        if (typeCount > 1) reportType = "COMBINED";

        if ("GENERAL".equals(reportType))
            throw new IllegalArgumentException(
                    "Cannot identify report type. Supported: BLOOD, ECG, THYROID, URINE, XRAY");

        Map<String, String> raw = isECG
                ? imageValueExtractor.extract(text, "ECG")
                : extractor.extractValues(text);

        if (raw == null || raw.isEmpty())
            throw new IllegalArgumentException("No medical values extracted from this PDF");

        String diagnosis = extractor.extractDiagnosis(text, reportType);

        List<ReportParameter> params = new ArrayList<>();
        int high = 0, low = 0, normal = 0;

        for (Map.Entry<String, String> e : raw.entrySet()) {
            String k = e.getKey(), v = e.getValue();
            if (SKIP_FIELDS.contains(k)) continue;
            try {
                if ("Blood Pressure".equals(k)) {
                    String bpStatus = referenceRange.getBPStatus(v);
                    if ("HIGH".equals(bpStatus)) high++;
                    else if ("NORMAL".equals(bpStatus)) normal++;
                    params.add(ReportParameter.of(k, v, "mmHg",
                            referenceRange.getBPRange(), bpStatus, "MER"));
                    continue;
                }
                double num = Double.parseDouble(v.replaceAll("[^0-9.]", ""));
                double[] range = referenceRange.getRange(k, gender);
                String status = range == null ? "N/A"
                        : referenceRange.getStatus(k, num, gender);
                if ("HIGH".equals(status)) high++;
                else if ("LOW".equals(status)) low++;
                else if ("NORMAL".equals(status)) normal++;
                params.add(ReportParameter.of(k, v,
                        referenceRange.getUnit(k),
                        referenceRange.formatRange(range),
                        status, referenceRange.getCategory(k)));
            } catch (NumberFormatException ex) {
                params.add(ReportParameter.of(k, v, "", "N/A", "INFO", "INFO"));
            }
        }

        long ts = System.currentTimeMillis() % 10000;
        String reportId = reportType + "_" + LocalDate.now() + "_" + ts;
        byte[] pdfBytes = toBytes(doc);

        // upsert patient
        AnalyzedPatient patient = patientRepo.findByPatientId(patientId)
                .orElseGet(() -> {
                    AnalyzedPatient p = new AnalyzedPatient();
                    p.setPatientId(patientId);
                    return p;
                });
        patient.setPatientName(name);
        patient.setGender(gender);
        patient.setAge(age);
        patientRepo.save(patient);

        // save report
        AnalyzedReport ar = new AnalyzedReport();
        ar.setReportId(reportId);
        ar.setPatientId(patientId);
        ar.setReportType(reportType);
        ar.setReportDate(LocalDate.now().toString());
        ar.setSourceType(srcType);
        ar.setDiagnosis(diagnosis);
        ar.setParametersJson(mapper.writeValueAsString(params));
        ar.setPdfData(pdfBytes);
        ar.setTotalParameters(params.size());
        ar.setHighCount(high);
        ar.setLowCount(low);
        ar.setNormalCount(normal);
        ar.setUnknownCount(params.size() - high - low - normal);
        reportRepo.save(ar);

        return toReport(ar, name, gender, age);
    }

    public List<Map<String, String>> getAllPatients() {
        List<Map<String, String>> list = new ArrayList<>();
        for (AnalyzedPatient p : patientRepo.findAll()) {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("patientId", p.getPatientId());
            m.put("patientName", p.getPatientName());
            m.put("gender", p.getGender());
            m.put("age", String.valueOf(p.getAge()));
            list.add(m);
        }
        return list;
    }

    public Map<String, String> getPatientInfo(String id) {
        return patientRepo.findByPatientId(id).map(p -> {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("patientId", p.getPatientId());
            m.put("patientName", p.getPatientName());
            m.put("gender", p.getGender());
            m.put("age", String.valueOf(p.getAge()));
            return m;
        }).orElse(null);
    }

    public void deletePatient(String id) {
        reportRepo.deleteByPatientId(id);
        patientRepo.deleteByPatientId(id);
    }

    public List<PatientReport> getPatientReports(String id) {
        List<PatientReport> list = new ArrayList<>();
        AnalyzedPatient patient = patientRepo.findByPatientId(id).orElse(null);
        if (patient == null) return list;
        for (AnalyzedReport ar : reportRepo.findByPatientIdOrderByCreatedAtDesc(id))
            list.add(toReport(ar, patient.getPatientName(), patient.getGender(), patient.getAge()));
        return list;
    }

    public PatientReport getReport(String id, String reportId) {
        AnalyzedPatient patient = patientRepo.findByPatientId(id).orElse(null);
        if (patient == null) return null;
        return reportRepo.findByPatientIdAndReportId(id, reportId)
                .map(ar -> toReport(ar, patient.getPatientName(), patient.getGender(), patient.getAge()))
                .orElse(null);
    }

    public byte[] getReportPdf(String id, String reportId) {
        return reportRepo.findByPatientIdAndReportId(id, reportId)
                .map(AnalyzedReport::getPdfData).orElse(null);
    }

    public void deleteReport(String id, String reportId) {
        reportRepo.deleteByPatientIdAndReportId(id, reportId);
    }

    private PatientReport toReport(AnalyzedReport ar, String name, String gender, int age) {
        PatientReport r = new PatientReport();
        r.setPatientId(ar.getPatientId());
        r.setPatientName(name);
        r.setGender(gender);
        r.setAge(age);
        r.setReportId(ar.getReportId());
        r.setReportType(ar.getReportType());
        r.setReportDate(ar.getReportDate());
        r.setSourceType(ar.getSourceType());
        r.setDiagnosis(ar.getDiagnosis());
        r.setParameters(parseParams(ar.getParametersJson()));
        r.setTotalParameters(ar.getTotalParameters());
        r.setHighCount(ar.getHighCount());
        r.setLowCount(ar.getLowCount());
        r.setNormalCount(ar.getNormalCount());
        r.setUnknownCount(ar.getUnknownCount());
        r.setJsonFile(ar.getReportId() + ".json");
        r.setPdfFile(ar.getReportId() + ".pdf");
        r.setViewUrl(BASE_URL + "/" + ar.getPatientId() + "/reports/" + ar.getReportId() + "/view");
        r.setDownloadUrl(BASE_URL + "/" + ar.getPatientId() + "/reports/" + ar.getReportId() + "/download");
        r.setDeleteUrl(BASE_URL + "/" + ar.getPatientId() + "/reports/" + ar.getReportId());
        return r;
    }

    private List<ReportParameter> parseParams(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return mapper.readValue(json, new TypeReference<List<ReportParameter>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private byte[] toBytes(PDDocument doc) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PDDocument out = new PDDocument();
        for (PDPage p : doc.getPages()) out.addPage(p);
        out.save(bos);
        out.close();
        return bos.toByteArray();
    }

    // ---- validation / extraction (unchanged) ----

    private void validatePatientDetails(String text, String reqId,
                                         String reqName, String reqGender, int reqAge) {
        String lower = text.toLowerCase();
        String pdfId = findFirst(lower, ID_PATTERNS);
        String pdfName = findName(lower);
        String pdfGender = findFirst(lower, GENDER_PATTERNS);
        String pdfAge = findFirst(lower, AGE_PATTERNS);

        if (pdfId != null && !pdfId.equals(reqId.trim()))
            throw new IllegalArgumentException(
                    "Patient ID mismatch! PDF has: " + pdfId + " but you entered: " + reqId);
        if (pdfName != null) {
            String pn = norm(pdfName);
            String rn = norm(reqName);
            if (!pn.equals(rn))
                throw new IllegalArgumentException(
                        "Name mismatch! PDF has: " + pdfName.trim().toUpperCase()
                                + " but you entered: " + reqName.toUpperCase());
        }
        if (pdfGender != null) {
            String pg = expandGender(pdfGender);
            if (!pg.equals(reqGender.trim().toUpperCase()))
                throw new IllegalArgumentException(
                        "Gender mismatch! PDF says: " + pg + " but you entered: " + reqGender.trim().toUpperCase());
        }
        if (pdfAge != null) {
            try {
                int pa = Integer.parseInt(pdfAge.trim());
                if (pa != reqAge)
                    throw new IllegalArgumentException(
                            "Age mismatch! PDF says: " + pa + " but you entered: " + reqAge);
            } catch (NumberFormatException ignored) {}
        }
    }

    private String findName(String text) {
        for (String pat : NAME_PATTERNS) {
            Matcher m = Pattern.compile(pat,
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(text);
            while (m.find()) {
                String val = m.group(1).trim();
                val = val.replaceAll("\\s+", " ").trim();
                if (val.length() >= 3 && val.matches("[a-z][a-z\\s]+")) return val;
            }
        }
        return null;
    }

    private String expandGender(String g) {
        if (g == null) return "";
        String up = g.trim().toUpperCase();
        if (up.equals("M")) return "MALE";
        if (up.equals("F")) return "FEMALE";
        return up;
    }

    private String norm(String s) {
        return s == null ? "" : s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private String[] readAllPages(PDDocument doc) throws IOException {
        PDFTextStripper s = new PDFTextStripper();
        PDFRenderer r = new PDFRenderer(doc);
        StringBuilder b = new StringBuilder();
        String type = "TEXT";
        for (int i = 0; i < doc.getNumberOfPages(); i++) {
            s.setStartPage(i + 1);
            s.setEndPage(i + 1);
            String t = s.getText(doc).trim();
            if (!t.isEmpty()) {
                b.append(t.toLowerCase()).append("\n");
            } else {
                type = "IMAGE";
                b.append(imageExtractor.extract(r, i)).append("\n");
            }
        }
        return new String[]{b.toString(), type};
    }

    private void validateMedicalContent(String text) {
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("PDF is empty or unreadable");
        if (Arrays.stream(MEDICAL_KEYWORDS).noneMatch(text.toLowerCase()::contains))
            throw new IllegalArgumentException(
                    "This is not a medical report. Upload blood, ECG, urine, thyroid, or chest report");
    }

    private String findFirst(String text, String[] patterns) {
        for (String pat : patterns) {
            Matcher m = Pattern.compile(pat,
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(text);
            if (m.find()) {
                String val = m.group(1).trim();
                if (!val.isEmpty()) return val;
            }
        }
        return null;
    }
}
