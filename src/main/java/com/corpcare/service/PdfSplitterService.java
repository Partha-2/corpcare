package com.corpcare.service;

import com.corpcare.config.ReportSplitConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfSplitterService {

    public List<SplitReportResult> splitReport(byte[] originalPdfBytes, Long employeeId) {
        List<SplitReportResult> results = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(originalPdfBytes)) {
            for (ReportSplitConfig.ReportSplitRule rule : ReportSplitConfig.SPLIT_RULES) {
                PDDocument splitDoc = new PDDocument();
                for (int i = rule.getStartPage(); i <= rule.getEndPage(); i++) {
                    splitDoc.addPage(document.getPage(i - 1));
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                splitDoc.save(baos);
                splitDoc.close();
                results.add(new SplitReportResult(rule.getReportType(), baos.toByteArray(), employeeId));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to split PDF", e);
        }
        return results;
    }

    public static class SplitReportResult {
        private final String reportType;
        private final byte[] pdfBytes;
        private final Long employeeId;

        public SplitReportResult(String reportType, byte[] pdfBytes, Long employeeId) {
            this.reportType = reportType;
            this.pdfBytes = pdfBytes;
            this.employeeId = employeeId;
        }

        public String getReportType() { return reportType; }
        public byte[] getPdfBytes() { return pdfBytes; }
        public Long getEmployeeId() { return employeeId; }
    }
}