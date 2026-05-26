package com.corpcare.service;

import com.corpcare.dto.HealthReportResponse;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfExtractionService {

    public HealthReportResponse extract(MultipartFile file) throws Exception {
        String text;
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
        }

        HealthReportResponse r = new HealthReportResponse();
        r.setName(extract(text, "(?:Name|Patient Name|Patient)\\s*[:.]?\\s*([A-Za-z\\s.]+)", 1));
        r.setAge(extract(text, "(?:Age)\\s*[:.]?\\s*(\\d{1,3})", 1));
        r.setSex(extract(text, "(?:Sex|Gender)\\s*[:.]?\\s*(Male|Female|M|F)", 1));
        r.setBloodGroup(extract(text, "(?:Blood Group|Blood Type|B Group|Blood Grp)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)", 1));
        r.setHeight(extract(text, "(?:Height|Height cm|Height in cm)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setWeight(extract(text, "(?:Weight|Weight kg|Weight in kg)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setBloodPressureSystolic(extract(text, "(?:Blood Pressure|BP|B.P)\\s*[:.]?\\s*(\\d{2,3})\\s*[/\\\\]", 1));
        r.setBloodPressureDiastolic(extract(text, "(?:Blood Pressure|BP|B.P)\\s*[:.]?\\s*\\d{2,3}\\s*[/\\\\]\\s*(\\d{2,3})", 1));
        r.setBloodSugarFasting(extract(text, "(?:Blood Sugar.*Fasting|Fasting.*Sugar|Fasting Blood Sugar|FBS)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setBloodSugarPostPrandial(extract(text, "(?:Blood Sugar.*Post|Post.*Prandial|PPBS|Post Prandial)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setBloodSugarRandom(extract(text, "(?:Blood Sugar.*Random|Random Sugar|RBS|Random Blood Sugar)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setHemoglobin(extract(text, "(?:Hemoglobin|Hb|Haemoglobin|HGB)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setRbcCount(extract(text, "(?:RBC|RBC Count|Red Blood Cells|Red Cell Count)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setWbcCount(extract(text, "(?:WBC|WBC Count|White Blood Cells|Total WBC|White Cell Count)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setPlateletCount(extract(text, "(?:Platelet|Platelets|PLT|Platelet Count|Thrombocyte)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setTotalCholesterol(extract(text, "(?:Total Cholesterol|Cholesterol|Serum Cholesterol)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setHdlCholesterol(extract(text, "(?:HDL|HDL Cholesterol|Good Cholesterol)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setLdlCholesterol(extract(text, "(?:LDL|LDL Cholesterol|Bad Cholesterol)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setTriglycerides(extract(text, "(?:Triglycerides|Triglyceride|TG|TGL)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setSerumCreatinine(extract(text, "(?:Creatinine|Serum Creatinine|Creat)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setUrea(extract(text, "(?:Urea|Blood Urea|BUN|Serum Urea)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setUricAcid(extract(text, "(?:Uric Acid|UA|Serum Uric Acid)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setPulseRate(extract(text, "(?:Pulse|Pulse Rate|Heart Rate|HR)\\s*[:.]?\\s*(\\d+)", 1));
        r.setOxygenSaturation(extract(text, "(?:Oxygen|SpO2|O2 Sat|Oxygen Saturation)\\s*[:.]?\\s*(\\d+)", 1));
        r.setTemperature(extract(text, "(?:Temperature|Temp|Body Temp)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setVitaminD(extract(text, "(?:Vitamin D|Vit D|25-OH Vitamin D|25 Hydroxy Vitamin D)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setVitaminB12(extract(text, "(?:Vitamin B12|Vit B12|B12|Cobalamin)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setTsh(extract(text, "(?:TSH|Thyroid.*TSH|Thyroid Stimulating|Thyrotropin)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setEsr(extract(text, "(?:ESR|Erythrocyte Sedimentation|Sed Rate)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setTotalBilirubin(extract(text, "(?:Total Bilirubin|Bilirubin|Bilirubin Total)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));
        r.setTotalProtein(extract(text, "(?:Total Protein|Protein Total|Serum Protein)\\s*[:.]?\\s*(\\d+\\.?\\d*)", 1));

        validateAll(r);
        return r;
    }

    private String extract(String text, String regex, int group) {
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(group).trim() : "";
    }

    private void validateAll(HealthReportResponse r) {
        double h = parseDouble(r.getHeight());
        double w = parseDouble(r.getWeight());
        double bps = parseDouble(r.getBloodPressureSystolic());
        double bpd = parseDouble(r.getBloodPressureDiastolic());
        double hb = parseDouble(r.getHemoglobin());
        double chol = parseDouble(r.getTotalCholesterol());
        double creat = parseDouble(r.getSerumCreatinine());
        double fbs = parseDouble(r.getBloodSugarFasting());

        if (h > 0) {
            r.setBmi(String.format("%.1f", w / ((h / 100.0) * (h / 100.0))));
            if (h < 150) r.setHeightStatus("Below Healthy Range");
            else if (h > 200) r.setHeightStatus("Above Healthy Range");
            else r.setHeightStatus("Normal");
        } else r.setHeightStatus("N/A");

        if (h > 0 && w > 0) {
            double hm = h / 100.0;
            double minW = 18.5 * hm * hm;
            double maxW = 24.9 * hm * hm;
            r.setRecommendedWeightMin(String.format("%.1f", minW));
            r.setRecommendedWeightMax(String.format("%.1f", maxW));
            if (w < minW) r.setWeightStatus("Below Required Range");
            else if (w > maxW) r.setWeightStatus("Above Required Range");
            else r.setWeightStatus("Healthy");
        } else r.setWeightStatus("N/A");

        if (bps > 0 && bpd > 0) {
            if (bps < 100 || bpd < 60) r.setBpStatus("Low");
            else if (bps > 140 || bpd > 90) r.setBpStatus("High");
            else r.setBpStatus("Normal");
        } else r.setBpStatus("N/A");

        if (fbs > 0) {
            if (fbs < 70) r.setSugarStatus("Low");
            else if (fbs > 126) r.setSugarStatus("High");
            else r.setSugarStatus("Normal");
        } else r.setSugarStatus("N/A");

        String sex = r.getSex();
        boolean isMale = "M".equalsIgnoreCase(sex) || "Male".equalsIgnoreCase(sex);
        if (hb > 0) {
            if (isMale) r.setHemoglobinStatus(hb < 13.5 ? "Low" : hb > 17.5 ? "High" : "Normal");
            else r.setHemoglobinStatus(hb < 12.0 ? "Low" : hb > 15.5 ? "High" : "Normal");
        } else r.setHemoglobinStatus("N/A");

        if (chol > 0) {
            if (chol < 125) r.setCholesterolStatus("Low");
            else if (chol > 200) r.setCholesterolStatus("High");
            else r.setCholesterolStatus("Normal");
        } else r.setCholesterolStatus("N/A");

        if (creat > 0) {
            if (isMale) r.setCreatinineStatus(creat < 0.7 ? "Low" : creat > 1.3 ? "High" : "Normal");
            else r.setCreatinineStatus(creat < 0.6 ? "Low" : creat > 1.1 ? "High" : "Normal");
        } else r.setCreatinineStatus("N/A");
    }

    private double parseDouble(String s) {
        if (s == null || s.isBlank()) return 0;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
    }
}
