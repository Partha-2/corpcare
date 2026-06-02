package com.corpcare.controller;

import com.corpcare.config.JwtUtil;
import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.entity.Client;
import com.corpcare.entity.Employee;
import com.corpcare.entity.Hospital;
import com.corpcare.exception.BusinessException;
import com.corpcare.repository.ClientRepository;
import com.corpcare.repository.EmployeeRepository;
import com.corpcare.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final HospitalRepository hospitalRepository;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    public AuthController(JwtUtil jwtUtil, EmployeeRepository employeeRepository,
                          ClientRepository clientRepository, HospitalRepository hospitalRepository) {
        this.jwtUtil = jwtUtil;
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminLogin(@RequestBody Map<String, String> body) {
        if (!adminPassword.equals(body.get("password"))) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid password"));
        }
        String token = jwtUtil.generateToken("admin", SecurityUtil.ROLE_ADMIN, "Admin", 0L);
        return ResponseEntity.ok(ApiResponse.success("Login successful", Map.of("token", token, "name", "Admin")));
    }

    @PostMapping("/employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> employeeLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("employeeCode");
        Employee emp = employeeRepository.findByEmailAndEmployeeCode(email, code)
                .orElseThrow(() -> new BusinessException("Invalid email or employee code"));

        String token = jwtUtil.generateToken(emp.getEmail(), SecurityUtil.ROLE_EMPLOYEE, emp.getFullName(), emp.getId());
        return ResponseEntity.ok(ApiResponse.success("Login successful", Map.of(
                "token", token, "employee", emp
        )));
    }

    @PostMapping("/client")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clientLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Client client = clientRepository.findByContactEmail(email)
                .orElseThrow(() -> new BusinessException("Client not found"));

        String stored = client.getPassword();
        if (stored == null || stored.isEmpty()) stored = "client123";
        if (!stored.equals(password)) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid password"));
        }
        String token = jwtUtil.generateToken(client.getContactEmail(), SecurityUtil.ROLE_CLIENT, client.getCompanyName(), client.getId());
        return ResponseEntity.ok(ApiResponse.success("Login successful", Map.of(
                "token", token, "client", client
        )));
    }

    @PostMapping("/hospital")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hospitalLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        Hospital hospital = hospitalRepository.findByContactEmail(email)
                .orElseThrow(() -> new BusinessException("Hospital not found"));

        String stored = hospital.getPassword();
        if (stored == null || stored.isEmpty()) stored = "hospital123";
        if (!stored.equals(password)) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid password"));
        }
        String token = jwtUtil.generateToken(hospital.getContactEmail(), SecurityUtil.ROLE_HOSPITAL, hospital.getHospitalName(), hospital.getId());
        return ResponseEntity.ok(ApiResponse.success("Login successful", Map.of(
                "token", token, "hospital", hospital
        )));
    }
}
