package com.corpcare.repository;

import com.corpcare.entity.EmployeeVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeVitalsRepository extends JpaRepository<EmployeeVitals, Long> {
    Optional<EmployeeVitals> findByEmployeeId(Long employeeId);
}
