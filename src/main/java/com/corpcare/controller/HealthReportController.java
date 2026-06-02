package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.HealthAnalysisReport;
import com.corpcare.entity.Employee;
import com.corpcare.entity.ReportDetail;
import com.corpcare.repository.ReportDetailRepository;
import com.corpcare.service.EmployeeService;
import com.corpcare.service.PdfExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthReportController {

    private final PdfExtractionService pdfExtractionService;
    private final EmployeeService employeeService;
    private final ReportDetailRepository reportDetailRepository;

    public HealthReportController(PdfExtractionService pdfExtractionService,
                                   EmployeeService employeeService,
                                   ReportDetailRepository reportDetailRepository) {
        this.pdfExtractionService = pdfExtractionService;
        this.employeeService = employeeService;
        this.reportDetailRepository = reportDetailRepository;
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

    @GetMapping("/report/{employeeCode}")
    public ResponseEntity<ApiResponse<List<ReportDetail>>> getReportByEmployeeCode(
            @PathVariable String employeeCode) {
        Employee employee = employeeService.getByEmployeeCode(employeeCode);
        List<ReportDetail> reports = reportDetailRepository
                .findByEmployeeIdOrderByCreatedAtDesc(employee.getId());
        return ResponseEntity.ok(ApiResponse.success("Health report fetched", reports));
    }
}
