package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.MedicalReportDTO;
import com.corpcare.entity.MedicalReport;
import com.corpcare.service.MedicalReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-reports")
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    @PostMapping("/upload/{employeeId}")
    public ResponseEntity<ApiResponse<?>> uploadReport(
            @PathVariable Long employeeId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {

        List<MedicalReport> reports = medicalReportService.uploadAndSplitReport(file, employeeId, uploadedBy);
        return ResponseEntity.ok(ApiResponse.success(
                "Report split into " + reports.size() + " PDFs successfully",
                reports.stream().map(MedicalReportDTO::fromEntity).collect(Collectors.toList())
        ));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<MedicalReportDTO>>> getEmployeeReports(
            @PathVariable Long employeeId) {

        List<MedicalReport> reports = medicalReportService.getReportsByEmployee(employeeId);
        List<MedicalReportDTO> dtos = reports.stream()
                .map(MedicalReportDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Reports fetched successfully", dtos));
    }

    @GetMapping("/download/{reportId}/employee/{employeeId}")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long reportId,
            @PathVariable Long employeeId) {

        MedicalReport report = medicalReportService.getReport(reportId, employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getReportType() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report.getPdfData());
    }

    @GetMapping("/view/{reportId}/employee/{employeeId}")
    public ResponseEntity<byte[]> viewReport(
            @PathVariable Long reportId,
            @PathVariable Long employeeId) {

        MedicalReport report = medicalReportService.getReport(reportId, employeeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + report.getReportType() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report.getPdfData());
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<ApiResponse<Void>> deleteReport(@PathVariable Long reportId) {
        medicalReportService.deleteReport(reportId);
        return ResponseEntity.ok(ApiResponse.success("Report deleted successfully", null));
    }
}
