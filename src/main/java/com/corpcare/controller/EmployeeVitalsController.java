package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.EmployeeVitalsRequest;
import com.corpcare.entity.EmployeeVitals;
import com.corpcare.service.EmployeeVitalsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees/{employeeId}/vitals")
public class EmployeeVitalsController {

    private final EmployeeVitalsService vitalsService;

    public EmployeeVitalsController(EmployeeVitalsService vitalsService) {
        this.vitalsService = vitalsService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeVitals>> saveVitals(
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeVitalsRequest request) {
        EmployeeVitals vitals = vitalsService.saveOrUpdateVitals(employeeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vitals saved successfully", vitals));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeVitals>> getVitals(@PathVariable Long employeeId) {
        EmployeeVitals vitals = vitalsService.getVitalsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Vitals fetched successfully", vitals));
    }
}
