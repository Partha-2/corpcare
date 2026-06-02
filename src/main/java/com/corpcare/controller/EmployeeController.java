package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.entity.Employee;
import com.corpcare.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.corpcare.config.SecurityUtil.ROLE_ADMIN;
import static com.corpcare.config.SecurityUtil.ROLE_CLIENT;
import static com.corpcare.config.SecurityUtil.ROLE_EMPLOYEE;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Employee>> verifyEmployee(@RequestBody Map<String, String> body) {
        Employee employee = employeeService.verifyEmployee(
                body.get("email"), body.get("employeeCode")
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", employee));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<Employee>> getEmployee(@PathVariable Long employeeId) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        var user = SecurityUtil.requireAuthenticated();
        String role = user.role();
        if (ROLE_ADMIN.equals(role)) {
            return ResponseEntity.ok(ApiResponse.success("Employee fetched", employee));
        }
        if (ROLE_CLIENT.equals(role)) {
            if (!employee.getClient().getId().equals(user.userId())) {
                throw new AccessDeniedException("Access denied");
            }
            return ResponseEntity.ok(ApiResponse.success("Employee fetched", employee));
        }
        if (ROLE_EMPLOYEE.equals(role)) {
            if (!user.userId().equals(employeeId)) {
                throw new AccessDeniedException("Access denied");
            }
            return ResponseEntity.ok(ApiResponse.success("Employee fetched", employee));
        }
        throw new AccessDeniedException("Access denied");
    }
}
