package com.corpcare.service;

import com.corpcare.dto.ReportAnalysisResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportAnalyzerServiceTest {

    private final ReportAnalyzerService service = new ReportAnalyzerService();

    @Test
    void testShivaniPdf() throws Exception {
        var file = new java.io.File("src/test/resources/vendor_report_2.pdf");
        if (!file.exists()) { System.out.println("SKIP: no PDF"); return; }

        ReportAnalysisResult r = service.analyze(java.nio.file.Files.readAllBytes(file.toPath()));
        String vendor = r.getVendor();
        System.out.println("Vendor: " + vendor);

        String name = r.getPatient().getName();
        String age = r.getPatient().getAge();
        String sex = r.getPatient().getSex();
        String date = r.getPatient().getDate();
        System.out.println("Patient: " + name + " / " + age + " / " + sex + " / " + date);

        int count = r.getParsedCount();
        System.out.println("Parsed: " + count + "/20  Confidence: " + r.getConfidence());

        for (var p : r.getParameters()) {
            System.out.printf("  %-20s = %-12s [%s]%n", p.getName(), p.getValue(), p.getStatus());
        }
        for (var a : r.getAlerts()) {
            System.out.println("  ALERT: " + a.getParameter() + " " + a.getDirection() + " - " + a.getMessage());
        }

        assertTrue(vendor.contains("Shivani"), "Expected Shivani vendor");
        assertFalse(name.isEmpty(), "Name should not be empty");
        assertFalse(age.isEmpty(), "Age should not be empty");
        assertFalse(sex.isEmpty(), "Sex should not be empty");
        assertTrue(count >= 16, "Expected >= 16/20 params extracted, got " + count);
    }

    @Test
    void testStarLabPdf() throws Exception {
        var file = new java.io.File("src/test/resources/vendor_report.pdf");
        if (!file.exists()) { System.out.println("SKIP: no PDF"); return; }

        ReportAnalysisResult r = service.analyze(java.nio.file.Files.readAllBytes(file.toPath()));
        String vendor = r.getVendor();
        System.out.println("Vendor: " + vendor);

        String name = r.getPatient().getName();
        String age = r.getPatient().getAge();
        System.out.println("Patient: " + name + " / " + age);

        int count = r.getParsedCount();
        System.out.println("Parsed: " + count + "/20  Confidence: " + r.getConfidence());

        for (var p : r.getParameters()) {
            System.out.printf("  %-20s = %-12s [%s]%n", p.getName(), p.getValue(), p.getStatus());
        }
        for (var a : r.getAlerts()) {
            System.out.println("  ALERT: " + a.getParameter() + " " + a.getDirection() + " - " + a.getMessage());
        }

        assertTrue(vendor.contains("Star Lab"), "Expected Star Lab vendor, got: " + vendor);
        assertTrue(count >= 16, "Expected >= 16/20 params extracted, got " + count);
    }
}
