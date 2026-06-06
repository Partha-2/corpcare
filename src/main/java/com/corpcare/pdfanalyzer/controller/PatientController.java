package com.corpcare.pdfanalyzer.controller;

import com.corpcare.pdfanalyzer.model.response.PatientReport;
import com.corpcare.pdfanalyzer.service.PatientReportService;
import com.corpcare.pdfsplit.model.response.ApiResponse;
import com.corpcare.pdfsplit.service.pdf.PdfValidatorService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/patient")
public class PatientController {

    private final PdfValidatorService validator;
    private final PatientReportService patientService;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<PatientReport>> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("patientId") String patientId,
            @RequestParam("name") String name,
            @RequestParam("gender") String gender,
            @RequestParam("age") int age) {

        if (file == null || file.isEmpty())
            return bad("Please upload a PDF file");
        String fn = file.getOriginalFilename();
        if (fn == null || !fn.toLowerCase().endsWith(".pdf"))
            return bad("Only PDF files are allowed. Please upload a .pdf file");
        if (file.getSize() > 50L * 1024 * 1024)
            return bad("File too large. Maximum allowed size is 50MB");

        String pid = patientId == null ? "" : patientId.trim();
        if (pid.isEmpty()) return bad("Patient ID is required");
        if (!pid.matches("[0-9]{3,10}"))
            return bad("Invalid Patient ID '" + pid + "'. Numbers only, 3-10 digits (e.g. 68382)");

        String nm = name == null ? "" : name.trim();
        if (nm.isEmpty()) return bad("Patient name is required");
        if (nm.length() < 3) return bad("Name too short. Minimum 3 characters");
        if (nm.length() > 60) return bad("Name too long. Maximum 60 characters");
        if (!nm.matches("[a-zA-Z\\s.'-]+"))
            return bad("Invalid name '" + nm + "'. Use letters and spaces only");

        String gen = gender == null ? "" : gender.trim().toUpperCase();
        if (gen.isEmpty()) return bad("Gender is required");
        if (!gen.equals("MALE") && !gen.equals("FEMALE"))
            return bad("Invalid gender '" + gender.trim() + "'. Use MALE or FEMALE only");

        if (age < 1) return bad("Invalid age " + age + ". Minimum age is 1");
        if (age > 120) return bad("Invalid age " + age + ". Maximum age is 120");

        try {
            PDDocument doc = validator.validateAndOpen(file);
            PatientReport result = patientService.analyze(doc, pid, nm, gen, age);
            doc.close();
            return ResponseEntity.ok(
                    ApiResponse.success("Report analyzed successfully", result));
        } catch (IllegalArgumentException e) {
            return bad(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.failure("Analysis failed: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> list() {
        try {
            List<Map<String, String>> p = patientService.getAllPatients();
            return ResponseEntity.ok(ApiResponse.success(
                    p.isEmpty() ? "No patients found"
                            : p.size() + " patient(s) found", p));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPatient(
            @PathVariable String patientId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        try {
            Map<String, String> p = patientService.getPatientInfo(patientId);
            return p == null ? notFound("Patient not found: " + patientId)
                    : ResponseEntity.ok(ApiResponse.success("Patient found", p));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<ApiResponse<String>> deletePatient(
            @PathVariable String patientId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        try {
            if (patientService.getPatientInfo(patientId) == null)
                return notFound("Patient not found: " + patientId);
            patientService.deletePatient(patientId);
            return ResponseEntity.ok(
                    ApiResponse.success("Patient " + patientId + " deleted", null));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    @GetMapping("/{patientId}/reports")
    public ResponseEntity<ApiResponse<List<PatientReport>>> getReports(
            @PathVariable String patientId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        try {
            if (patientService.getPatientInfo(patientId) == null)
                return notFound("Patient not found: " + patientId);
            List<PatientReport> r = patientService.getPatientReports(patientId);
            return ResponseEntity.ok(ApiResponse.success(
                    r.isEmpty() ? "No reports found"
                            : r.size() + " report(s) found", r));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    @GetMapping("/{patientId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<PatientReport>> getReport(
            @PathVariable String patientId,
            @PathVariable String reportId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        try {
            PatientReport r = patientService.getReport(patientId, reportId);
            return r == null ? notFound("Report not found: " + reportId)
                    : ResponseEntity.ok(ApiResponse.success("Report found", r));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    @GetMapping("/{patientId}/reports/{reportId}/view")
    public ResponseEntity<?> viewReport(
            @PathVariable String patientId,
            @PathVariable String reportId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        byte[] data = patientService.getReportPdf(patientId, reportId);
        if (data == null) return notFound("PDF not found: " + reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + reportId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/{patientId}/reports/{reportId}/download")
    public ResponseEntity<?> downloadReport(
            @PathVariable String patientId,
            @PathVariable String reportId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        byte[] data = patientService.getReportPdf(patientId, reportId);
        if (data == null) return notFound("PDF not found: " + reportId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + reportId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @DeleteMapping("/{patientId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<String>> deleteReport(
            @PathVariable String patientId,
            @PathVariable String reportId) {
        if (invalidId(patientId)) return bad("Invalid Patient ID format");
        try {
            if (patientService.getReport(patientId, reportId) == null)
                return notFound("Report not found: " + reportId);
            patientService.deleteReport(patientId, reportId);
            return ResponseEntity.ok(
                    ApiResponse.success("Report " + reportId + " deleted", null));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    private boolean invalidId(String id) {
        return id == null || !id.matches("[0-9]{3,10}");
    }

    private <T> ResponseEntity<ApiResponse<T>> bad(String m) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(m));
    }

    private <T> ResponseEntity<ApiResponse<T>> notFound(String m) {
        return ResponseEntity.status(404).body(ApiResponse.failure(m));
    }

    private <T> ResponseEntity<ApiResponse<T>> err(String m) {
        return ResponseEntity.internalServerError().body(ApiResponse.failure(m));
    }
}
