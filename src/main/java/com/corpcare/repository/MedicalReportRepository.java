package com.corpcare.repository;

import com.corpcare.entity.MedicalReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    List<MedicalReport> findByEmployeeId(Long employeeId);

    List<MedicalReport> findByEmployeeIdAndReportType(Long employeeId, String reportType);

    Optional<MedicalReport> findByIdAndEmployeeId(Long id, Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
