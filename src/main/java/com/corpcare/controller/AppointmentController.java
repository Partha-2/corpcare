package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.AppointmentRequest;
import com.corpcare.entity.Appointment;
import com.corpcare.entity.Employee;
import com.corpcare.service.AppointmentService;
import com.corpcare.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final EmployeeService employeeService;

    public AppointmentController(AppointmentService appointmentService, EmployeeService employeeService) {
        this.appointmentService = appointmentService;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> bookAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        var user = SecurityUtil.getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        if ("CLIENT".equals(user.role())) {
            Employee emp = employeeService.getEmployeeById(request.getEmployeeId());
            if (!emp.getClient().getId().equals(user.userId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        if ("EMPLOYEE".equals(user.role())) {
            if (!user.userId().equals(request.getEmployeeId())) {
                throw new AccessDeniedException("Access denied");
            }
        }
        Appointment appointment = appointmentService.bookAppointment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment booked successfully", appointment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Appointment>>> getAllAppointments() {
        var user = SecurityUtil.getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        List<Appointment> appointments;
        if ("ADMIN".equals(user.role())) {
            appointments = appointmentService.getAllAppointments();
        } else if ("CLIENT".equals(user.role())) {
            appointments = appointmentService.getAppointmentsByClient(user.userId());
        } else if ("HOSPITAL".equals(user.role())) {
            appointments = appointmentService.getAppointmentsByHospital(user.userId());
        } else if ("EMPLOYEE".equals(user.role())) {
            appointments = appointmentService.getAppointmentsByEmployee(user.userId());
        } else {
            throw new AccessDeniedException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched", appointments));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<Appointment>>> getEmployeeAppointments(
            @PathVariable Long employeeId) {
        var user = SecurityUtil.getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        if ("ADMIN".equals(user.role())) {
            // ok
        } else if ("EMPLOYEE".equals(user.role())) {
            if (!user.userId().equals(employeeId)) throw new AccessDeniedException("Access denied");
        } else if ("CLIENT".equals(user.role())) {
            Employee emp = employeeService.getEmployeeById(employeeId);
            if (!emp.getClient().getId().equals(user.userId())) throw new AccessDeniedException("Access denied");
        } else {
            throw new AccessDeniedException("Access denied");
        }
        List<Appointment> appointments = appointmentService.getAppointmentsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Appointments fetched", appointments));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<ApiResponse<List<Appointment>>> getHospitalAppointments(
            @PathVariable Long hospitalId) {
        var user = SecurityUtil.getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        if ("ADMIN".equals(user.role())) {
            // ok
        } else if ("HOSPITAL".equals(user.role())) {
            if (!user.userId().equals(hospitalId)) throw new AccessDeniedException("Access denied");
        } else {
            throw new AccessDeniedException("Access denied");
        }
        List<Appointment> appointments = appointmentService.getAppointmentsByHospital(hospitalId);
        return ResponseEntity.ok(ApiResponse.success("Hospital appointments fetched", appointments));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        var user = SecurityUtil.getCurrentUser();
        if (user == null) throw new AccessDeniedException("Not authenticated");
        if ("ADMIN".equals(user.role())) {
            // ok
        } else if ("EMPLOYEE".equals(user.role())) {
            if (!user.userId().equals(appointment.getEmployee().getId())) {
                throw new AccessDeniedException("Access denied");
            }
        } else if ("HOSPITAL".equals(user.role())) {
            if (!user.userId().equals(appointment.getSlot().getHospital().getId())) {
                throw new AccessDeniedException("Access denied");
            }
        } else if ("CLIENT".equals(user.role())) {
            if (!user.userId().equals(appointment.getEmployee().getClient().getId())) {
                throw new AccessDeniedException("Access denied");
            }
        } else {
            throw new AccessDeniedException("Access denied");
        }
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", null));
    }
}
