package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.HealthReportResponse;
import com.corpcare.service.PdfExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/health-report")
public class HealthReportController {

    private final PdfExtractionService pdfExtractionService;

    public HealthReportController(PdfExtractionService pdfExtractionService) {
        this.pdfExtractionService = pdfExtractionService;
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
            HealthReportResponse report = pdfExtractionService.extract(file);
            return ResponseEntity.ok(ApiResponse.success("Report analyzed successfully", report.toResultMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Failed to analyze PDF: " + e.getMessage()));
        }
    }
}
