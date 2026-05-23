package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.AppointmentRequest;
import com.corpcare.entity.Appointment;
import com.corpcare.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        Appointment appointment = appointmentService.bookAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", appointment));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Appointment>>> getEmployeeAppointments(
            @PathVariable Long employeeId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched", appointments));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<ApiResponse<List<Appointment>>> getHospitalAppointments(
            @PathVariable Long hospitalId) {
        List<Appointment> appointments = appointmentService.getAppointmentsByHospital(hospitalId);
        return ResponseEntity.ok(ApiResponse.success("Hospital appointments fetched", appointments));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", null));
    }
}
