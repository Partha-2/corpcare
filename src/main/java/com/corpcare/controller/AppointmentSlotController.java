package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.SlotRequest;
import com.corpcare.entity.AppointmentSlot;
import com.corpcare.service.AppointmentSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/slots")
public class AppointmentSlotController {

    private final AppointmentSlotService slotService;

    public AppointmentSlotController(AppointmentSlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentSlot>> createSlot(
            @PathVariable Long hospitalId,
            @Valid @RequestBody SlotRequest request) {
        SecurityUtil.requireOwnership(hospitalId, "HOSPITAL");
        AppointmentSlot slot = slotService.createSlot(hospitalId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Slot created successfully", slot));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AppointmentSlot>>> getAvailableSlots(
            @PathVariable Long hospitalId,
            @RequestParam(required = false) String date) {
        LocalDate slotDate = date != null ? LocalDate.parse(date) : null;
        List<AppointmentSlot> slots = slotService.getAvailableSlots(hospitalId, slotDate);
        return ResponseEntity.ok(ApiResponse.success("Available slots fetched successfully", slots));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentSlot>>> getAllSlots(
            @PathVariable Long hospitalId) {
        SecurityUtil.requireOwnership(hospitalId, "HOSPITAL");
        List<AppointmentSlot> slots = slotService.getAllSlotsByHospital(hospitalId);
        return ResponseEntity.ok(ApiResponse.success("Slots fetched successfully", slots));
    }
}
