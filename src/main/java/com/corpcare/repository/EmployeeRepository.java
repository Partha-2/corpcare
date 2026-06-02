package com.corpcare.repository;

import com.corpcare.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByClientId(Long clientId);
    long countByClientId(Long clientId);
    java.util.Optional<Employee> findByEmailAndEmployeeCode(String email, String employeeCode);
    java.util.Optional<Employee> findByEmployeeCode(String employeeCode);
}
