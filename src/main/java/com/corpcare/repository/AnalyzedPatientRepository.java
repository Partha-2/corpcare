package com.corpcare.repository;

import com.corpcare.entity.AnalyzedPatient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalyzedPatientRepository extends JpaRepository<AnalyzedPatient, Long> {
    Optional<AnalyzedPatient> findByPatientId(String patientId);
    boolean existsByPatientId(String patientId);
    void deleteByPatientId(String patientId);
}
