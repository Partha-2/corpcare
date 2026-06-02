package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.HospitalRequest;
import com.corpcare.entity.Hospital;
import com.corpcare.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.corpcare.config.SecurityUtil.ROLE_HOSPITAL;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Hospital>> createHospital(@Valid @RequestBody HospitalRequest request) {
        SecurityUtil.requireAdmin();
        Hospital hospital = hospitalService.createHospital(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hospital registered successfully", hospital));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Hospital>>> getAllHospitals() {
        return ResponseEntity.ok(ApiResponse.success("Hospitals fetched successfully", hospitalService.getAllHospitals()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Hospital>> getMyHospital() {
        var user = SecurityUtil.requireAuthenticated();
        if (!ROLE_HOSPITAL.equals(user.role())) {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success("Hospital fetched successfully",
                hospitalService.getHospitalById(user.userId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Hospital>> updateHospital(@PathVariable Long id, @Valid @RequestBody HospitalRequest request) {
        SecurityUtil.requireOwnership(id, ROLE_HOSPITAL);
        Hospital hospital = hospitalService.updateHospital(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", hospital));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHospital(@PathVariable Long id) {
        SecurityUtil.requireAdmin();
        hospitalService.deleteHospital(id);
        return ResponseEntity.ok(ApiResponse.success("Hospital deleted successfully", null));
    }
}
