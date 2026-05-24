package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.DashboardStats;
import com.corpcare.repository.ClientRepository;
import com.corpcare.repository.HospitalRepository;
import com.corpcare.repository.EmployeeRepository;
import com.corpcare.repository.AppointmentSlotRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ClientRepository clientRepo;
    private final HospitalRepository hospitalRepo;
    private final EmployeeRepository employeeRepo;
    private final AppointmentSlotRepository slotRepo;

    public StatsController(ClientRepository clientRepo, HospitalRepository hospitalRepo,
                           EmployeeRepository employeeRepo, AppointmentSlotRepository slotRepo) {
        this.clientRepo = clientRepo;
        this.hospitalRepo = hospitalRepo;
        this.employeeRepo = employeeRepo;
        this.slotRepo = slotRepo;
    }

    @GetMapping
    public ApiResponse<DashboardStats> getStats() {
        DashboardStats stats = new DashboardStats(
            clientRepo.count(),
            hospitalRepo.count(),
            employeeRepo.count(),
            slotRepo.count()
        );
        return ApiResponse.success("Dashboard stats", stats);
    }
}
