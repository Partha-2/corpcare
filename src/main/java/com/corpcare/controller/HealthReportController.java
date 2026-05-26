package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.HealthAnalysisReport;
import com.corpcare.service.HealthRecommendationService;
import com.corpcare.service.PdfExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthReportController {

    private final PdfExtractionService pdfExtractionService;
    private final HealthRecommendationService recommendationService;

    public HealthReportController(PdfExtractionService pdfExtractionService,
                                   HealthRecommendationService recommendationService) {
        this.pdfExtractionService = pdfExtractionService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<Map<String, Object>>> analyzePdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No file uploaded"));
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only PDF files are accepted"));
        }
        try {
            HealthAnalysisReport report = pdfExtractionService.analyze(file);
            return ResponseEntity.ok(ApiResponse.success("Report analyzed successfully", report.toResultMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to analyze PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/report/{employeeId}")
    public ResponseEntity<ApiResponse<String>> getReport(@PathVariable String employeeId,
                                                          @RequestParam("name") String name,
                                                          @RequestParam("age") String age,
                                                          @RequestParam("sex") String sex,
                                                          @RequestParam("bloodGroup") String bloodGroup,
                                                          @RequestParam("vendor") String vendor) {
        String reportText = recommendationService.generateReportText(
            vendor, name, age, sex, bloodGroup, java.util.Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.success("Report generated", reportText));
    }
}
