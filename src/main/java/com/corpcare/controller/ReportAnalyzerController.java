package com.corpcare.controller;

import com.corpcare.dto.ReportAnalysisResult;
import com.corpcare.entity.ReportAnalysis;
import com.corpcare.entity.ReportDetail;
import com.corpcare.repository.ReportAnalysisRepository;
import com.corpcare.repository.ReportDetailRepository;
import com.corpcare.service.ReportAnalyzerService;
import com.corpcare.service.ReportDetailMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/report-analyzer")
public class ReportAnalyzerController {

    private final ReportAnalyzerService service;
    private final ReportAnalysisRepository repo;
    private final ReportDetailRepository detailRepo;

    public ReportAnalyzerController(ReportAnalyzerService service,
                                     ReportAnalysisRepository repo,
                                     ReportDetailRepository detailRepo) {
        this.service = service;
        this.repo = repo;
        this.detailRepo = detailRepo;
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
        return ResponseEntity.ok(detailRepo.findByEmployeeIdOrderByCreatedAtDesc(jwtUser.userId()));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<?> historyDetail(@PathVariable Long id, Authentication auth) {
        if (!(auth.getPrincipal() instanceof com.corpcare.config.JwtAuthFilter.JwtUser jwtUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        ReportDetail report = detailRepo.findById(id).orElse(null);
        if (report == null || !report.getEmployeeId().equals(jwtUser.userId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/history/{id}/parameters")
    public ResponseEntity<?> historyParameters(@PathVariable Long id, Authentication auth) {
        if (!(auth.getPrincipal() instanceof com.corpcare.config.JwtAuthFilter.JwtUser jwtUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        ReportDetail report = detailRepo.findById(id).orElse(null);
        if (report == null || !report.getEmployeeId().equals(jwtUser.userId())) {
            return ResponseEntity.status(404).body(Map.of("error", "Report not found"));
        }
        Map<String, Object> flat = new HashMap<>();
        flat.put("patientName", report.getPatientName());
        flat.put("patientAge", report.getPatientAge());
        flat.put("patientSex", report.getPatientSex());
        flat.put("vendor", report.getVendor());
        flat.put("haemoglobin", report.getHaemoglobin()); flat.put("haemoglobinStatus", report.getHaemoglobinStatus());
        flat.put("rbcCount", report.getRbcCount()); flat.put("rbcCountStatus", report.getRbcCountStatus());
        flat.put("pcvHct", report.getPcvHct()); flat.put("pcvHctStatus", report.getPcvHctStatus());
        flat.put("mcv", report.getMcv()); flat.put("mcvStatus", report.getMcvStatus());
        flat.put("mch", report.getMch()); flat.put("mchStatus", report.getMchStatus());
        flat.put("mchc", report.getMchc()); flat.put("mchcStatus", report.getMchcStatus());
        flat.put("rdwCv", report.getRdwCv()); flat.put("rdwCvStatus", report.getRdwCvStatus());
        flat.put("totalWbcCount", report.getTotalWbcCount()); flat.put("totalWbcCountStatus", report.getTotalWbcCountStatus());
        flat.put("neutrophils", report.getNeutrophils()); flat.put("neutrophilsStatus", report.getNeutrophilsStatus());
        flat.put("lymphocytes", report.getLymphocytes()); flat.put("lymphocytesStatus", report.getLymphocytesStatus());
        flat.put("monocytes", report.getMonocytes()); flat.put("monocytesStatus", report.getMonocytesStatus());
        flat.put("eosinophils", report.getEosinophils()); flat.put("eosinophilsStatus", report.getEosinophilsStatus());
        flat.put("basophils", report.getBasophils()); flat.put("basophilsStatus", report.getBasophilsStatus());
        flat.put("plateletCount", report.getPlateletCount()); flat.put("plateletCountStatus", report.getPlateletCountStatus());
        flat.put("esr", report.getEsr()); flat.put("esrStatus", report.getEsrStatus());
        flat.put("creatinine", report.getCreatinine()); flat.put("creatinineStatus", report.getCreatinineStatus());
        flat.put("urinePusCells", report.getUrinePusCells()); flat.put("urinePusCellsStatus", report.getUrinePusCellsStatus());
        flat.put("urineProtein", report.getUrineProtein()); flat.put("urineProteinStatus", report.getUrineProteinStatus());
        flat.put("urineSugar", report.getUrineSugar()); flat.put("urineSugarStatus", report.getUrineSugarStatus());
        flat.put("urineRbc", report.getUrineRbc()); flat.put("urineRbcStatus", report.getUrineRbcStatus());
        flat.put("criticalAlert", report.getCriticalAlert());
        flat.put("criticalAlertMessage", report.getCriticalAlertMessage());
        return ResponseEntity.ok(flat);
    }

    @GetMapping("/critical")
    public ResponseEntity<?> critical(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(detailRepo.findCriticalReports());
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

        ReportDetail detail = ReportDetailMapper.fromResult(result, employeeId, filename);
        detailRepo.save(detail);
    }
}
