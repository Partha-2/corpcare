package com.corpcare.controller;

import com.corpcare.dto.ReportAnalysisResult;
import com.corpcare.service.ReportAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/report-analyzer")
public class ReportAnalyzerController {

    private final ReportAnalyzerService service;

    public ReportAnalyzerController(ReportAnalyzerService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "No file uploaded"));
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Only PDF files are accepted"));
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "File exceeds 10MB limit"));
        }
        try {
            ReportAnalysisResult result = service.analyze(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }
}
