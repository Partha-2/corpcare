package com.corpcare.repository;

import com.corpcare.entity.ReportAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportAnalysisRepository extends JpaRepository<ReportAnalysis, Long> {
    List<ReportAnalysis> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
}
