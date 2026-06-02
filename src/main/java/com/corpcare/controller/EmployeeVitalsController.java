package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.EmployeeVitalsRequest;
import com.corpcare.entity.Employee;
import com.corpcare.entity.EmployeeVitals;
import com.corpcare.service.EmployeeService;
import com.corpcare.service.EmployeeVitalsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import static com.corpcare.config.SecurityUtil.ROLE_ADMIN;
import static com.corpcare.config.SecurityUtil.ROLE_CLIENT;
import static com.corpcare.config.SecurityUtil.ROLE_EMPLOYEE;

@RestController
@RequestMapping("/api/employees/{employeeId}/vitals")
public class EmployeeVitalsController {

    private final EmployeeVitalsService vitalsService;
    private final EmployeeService employeeService;

    public EmployeeVitalsController(EmployeeVitalsService vitalsService, EmployeeService employeeService) {
        this.vitalsService = vitalsService;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeVitals>> saveVitals(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeVitalsRequest request) {
        verifyAccess(employeeId);
        EmployeeVitals vitals = vitalsService.saveOrUpdateVitals(employeeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vitals saved successfully", vitals));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeVitals>> getVitals(@PathVariable Long employeeId) {
        verifyAccess(employeeId);
        EmployeeVitals vitals = vitalsService.getVitalsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Vitals fetched successfully", vitals));
    }

    private void verifyAccess(Long employeeId) {
        var user = SecurityUtil.requireAuthenticated();
        String role = user.role();
        if (ROLE_ADMIN.equals(role)) return;
        if (ROLE_EMPLOYEE.equals(role)) {
            if (!user.userId().equals(employeeId)) throw new AccessDeniedException("Access denied");
            return;
        }
        if (ROLE_CLIENT.equals(role)) {
            Employee emp = employeeService.getEmployeeById(employeeId);
            if (!emp.getClient().getId().equals(user.userId())) throw new AccessDeniedException("Access denied");
            return;
        }
        throw new AccessDeniedException("Access denied");
    }
}
