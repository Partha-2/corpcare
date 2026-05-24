package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verify(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (adminPassword.equals(password)) {
            return ApiResponse.success("Admin verified", Map.of("authenticated", true));
        }
        return ApiResponse.error("Invalid password");
    }
}
