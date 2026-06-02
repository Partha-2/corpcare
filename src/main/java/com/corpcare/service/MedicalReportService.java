package com.corpcare.service;

import com.corpcare.entity.MedicalReport;
import com.corpcare.repository.MedicalReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalReportService {

    private final PdfSplitterService pdfSplitterService;
    private final MedicalReportRepository medicalReportRepository;

    public MedicalReportService(PdfSplitterService pdfSplitterService, MedicalReportRepository medicalReportRepository) {
        this.pdfSplitterService = pdfSplitterService;
        this.medicalReportRepository = medicalReportRepository;
    }

    public List<MedicalReport> uploadAndSplitReport(MultipartFile file, Long employeeId, String uploadedBy) {
        try {
            byte[] bytes = file.getBytes();
            List<PdfSplitterService.SplitReportResult> splitResults =
                    pdfSplitterService.splitReport(bytes, employeeId);

            List<MedicalReport> reports = new ArrayList<>();
            for (PdfSplitterService.SplitReportResult result : splitResults) {
                MedicalReport report = new MedicalReport(
                        employeeId,
                        result.getReportType(),
                        file.getOriginalFilename(),
                        result.getPdfBytes(),
                        uploadedBy
                );
                reports.add(report);
            }

            return medicalReportRepository.saveAll(reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }
    }

    public List<MedicalReport> getReportsByEmployee(Long employeeId) {
        return medicalReportRepository.findByEmployeeId(employeeId);
    }

    public byte[] downloadReport(Long reportId, Long employeeId) {
        MedicalReport report = medicalReportRepository.findByIdAndEmployeeId(reportId, employeeId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return report.getPdfData();
    }

    public void deleteReport(Long reportId) {
        medicalReportRepository.deleteById(reportId);
    }
}
