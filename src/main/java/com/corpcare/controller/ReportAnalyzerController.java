package com.corpcare.controller;

import com.corpcare.dto.ReportAnalysisResult;
import com.corpcare.entity.ReportAnalysis;
import com.corpcare.repository.ReportAnalysisRepository;
import com.corpcare.service.ReportAnalyzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report-analyzer")
public class ReportAnalyzerController {

    private final ReportAnalyzerService service;
    private final ReportAnalysisRepository repo;

    public ReportAnalyzerController(ReportAnalyzerService service, ReportAnalysisRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are accepted"));
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds 10MB limit"));
        }
        try {
            ReportAnalysisResult result = service.analyze(file.getBytes());
            if (auth != null && auth.isAuthenticated()
                    && auth.getPrincipal() instanceof com.corpcare.config.JwtAuthFilter.JwtUser jwtUser) {
                saveReport(filename, result, jwtUser.userId());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }

    @PostMapping("/debug-text")
    public ResponseEntity<?> debugText(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }
        try {
            String text = service.extractTextOnly(file.getBytes());
            return ResponseEntity.ok(Map.of(
                "text", text,
                "length", String.valueOf(text.length())
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Extraction failed: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication auth) {
        if (!(auth.getPrincipal() instanceof com.corpcare.config.JwtAuthFilter.JwtUser jwtUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<ReportAnalysis> reports = repo.findByEmployeeIdOrderByCreatedAtDesc(jwtUser.userId());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> historyDetail(@PathVariable Long id, Authentication auth) {
        if (!(auth.getPrincipal() instanceof com.corpcare.config.JwtAuthFilter.JwtUser jwtUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        ReportAnalysis report = repo.findById(id).orElse(null);
        if (report == null || !report.getEmployeeId().equals(jwtUser.userId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        return ResponseEntity.ok(report);
    }

    private void saveReport(String filename, ReportAnalysisResult result, Long employeeId) {
        ReportAnalysis ra = new ReportAnalysis();
        ra.setEmployeeId(employeeId);
        ra.setFileName(filename);
        ra.setVendor(result.getVendor());
        ra.setPatientName(result.getPatient() != null ? result.getPatient().getName() : "");
        ra.setPatientAge(result.getPatient() != null ? result.getPatient().getAge() : "");
        ra.setPatientSex(result.getPatient() != null ? result.getPatient().getSex() : "");
        ra.setPatientDate(result.getPatient() != null ? result.getPatient().getDate() : "");
        ra.setParsedCount(result.getParsedCount());
        ra.setConfidence(result.getConfidence());
        try {
            ra.setReportData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result));
        } catch (Exception e) {
            ra.setReportData("{}");
        }
        repo.save(ra);
    }
}
