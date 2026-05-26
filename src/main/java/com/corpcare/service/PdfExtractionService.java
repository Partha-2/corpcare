package com.corpcare.service;

import com.corpcare.dto.HealthAnalysisReport;
import com.corpcare.dto.HealthParameter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class PdfExtractionService {

    private final VendorClassifierService vendorClassifier;
    private final TemplateMatchingEngine templateEngine;
    private final HealthRangeValidator rangeValidator;
    private final HealthRecommendationService recommendationService;

    public PdfExtractionService(VendorClassifierService vendorClassifier,
                                 TemplateMatchingEngine templateEngine,
                                 HealthRangeValidator rangeValidator,
                                 HealthRecommendationService recommendationService) {
        this.vendorClassifier = vendorClassifier;
        this.templateEngine = templateEngine;
        this.rangeValidator = rangeValidator;
        this.recommendationService = recommendationService;
    }

    public HealthAnalysisReport analyze(MultipartFile file) throws Exception {
        String text;
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(doc);
        }

        String vendorFormat = vendorClassifier.classify(text);

        Map<String, String> extracted = templateEngine.extract(text, vendorFormat);

        HealthAnalysisReport report = new HealthAnalysisReport();
        report.setVendorFormat(vendorFormat);
        report.setEmployeeName(extracted.getOrDefault("employeeName", "Not Available"));
        report.setAge(extracted.getOrDefault("age", "Not Available"));
        report.setSex(extracted.getOrDefault("sex", "Not Available"));
        report.setBloodGroup(extracted.getOrDefault("bloodGroup", "Not Available"));

        boolean isMale = "Male".equalsIgnoreCase(extracted.get("sex")) ||
                         "M".equalsIgnoreCase(extracted.get("sex"));

        String[] paramKeys = {"hemoglobin", "rbcCount", "wbcCount", "plateletCount", "esr",
            "creatinine", "urea", "bloodSugar", "sgpt", "sgot", "urinePH", "specificGravity",
            "urineProtein", "urineGlucose", "height", "weight"};

        for (String key : paramKeys) {
            HealthParameter hp = rangeValidator.validate(key, extracted.getOrDefault(key, "Not Available"), isMale);
            report.getParameters().add(hp);
        }

        List<String> notifications = recommendationService.generateNotifications(report.getParameters());
        report.setNotifications(notifications);

        return report;
    }
}
