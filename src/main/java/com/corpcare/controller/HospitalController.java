package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.HospitalRequest;
import com.corpcare.entity.Hospital;
import com.corpcare.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@Valid @RequestBody HospitalRequest request) {
        Hospital hospital = hospitalService.createHospital(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hospital registered successfully", hospital));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Hospital>>> getAllHospitals() {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(ApiResponse.success("Hospitals fetched successfully", hospitals));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @Valid @RequestBody HospitalRequest request) {
        Hospital hospital = hospitalService.updateHospital(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", hospital));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.ok(ApiResponse.success("Hospital deleted successfully", null));
    }
}
