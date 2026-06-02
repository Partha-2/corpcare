package com.corpcare.config;

import java.util.List;

public class ReportSplitConfig {

    public static class ReportSplitRule {
        private final String reportType;
        private final int startPage;
        private final int endPage;

        public ReportSplitRule(String reportType, int startPage, int endPage) {
            this.reportType = reportType;
            this.startPage = startPage;
            this.endPage = endPage;
        }

        public String getReportType() { return reportType; }
        public int getStartPage() { return startPage; }
        public int getEndPage() { return endPage; }
    }

    public static final List<ReportSplitRule> SPLIT_RULES = List.of(
        new ReportSplitRule("Complete Blood Count & Blood Glucose", 1, 3),
        new ReportSplitRule("Urine Routine & Microscopic Examination", 4, 5),
        new ReportSplitRule("Chest X-Ray PA View", 6, 7),
        new ReportSplitRule("Eye Checkup Vision & Refraction", 8, 8)
    );
}
