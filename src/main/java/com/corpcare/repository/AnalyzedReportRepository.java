package com.corpcare.repository;

import com.corpcare.entity.AnalyzedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalyzedReportRepository extends JpaRepository<AnalyzedReport, Long> {
    List<AnalyzedReport> findByPatientIdOrderByCreatedAtDesc(String patientId);
    Optional<AnalyzedReport> findByPatientIdAndReportId(String patientId, String reportId);
    boolean existsByPatientIdAndReportId(String patientId, String reportId);
    void deleteByPatientIdAndReportId(String patientId, String reportId);
    void deleteByPatientId(String patientId);
}
