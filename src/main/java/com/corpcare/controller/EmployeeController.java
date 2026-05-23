package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.EmployeeRequest;
import com.corpcare.entity.Employee;
import com.corpcare.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/api/employees/verify")
    public ResponseEntity<ApiResponse<Employee>> verifyEmployee(@RequestBody Map<String, String> body) {
        Employee employee = employeeService.verifyEmployee(
                body.get("email"), body.get("employeeCode")
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", employee));
    }

    @PostMapping("/api/clients/{clientId}/employees")
    public ResponseEntity<ApiResponse<Employee>> addEmployee(
            @PathVariable Long clientId,
            @Valid @RequestBody EmployeeRequest request) {
        Employee employee = employeeService.createEmployee(clientId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee added successfully", employee));
    }

    @GetMapping("/api/clients/{clientId}/employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployees(@PathVariable Long clientId) {
        List<Employee> employees = employeeService.getEmployeesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }
}
