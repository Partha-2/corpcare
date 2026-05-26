package com.corpcare.service;

import com.corpcare.dto.EmployeeVitalsRequest;
import com.corpcare.entity.Employee;
import com.corpcare.entity.EmployeeVitals;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.EmployeeVitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeVitalsService {

    private final EmployeeVitalsRepository vitalsRepository;
    private final EmployeeService employeeService;

    public EmployeeVitalsService(EmployeeVitalsRepository vitalsRepository, EmployeeService employeeService) {
        this.vitalsRepository = vitalsRepository;
        this.employeeService = employeeService;
    }

    @Transactional
    public EmployeeVitals saveOrUpdateVitals(Long employeeId, EmployeeVitalsRequest request) {
        Employee employee = employeeService.getEmployeeById(employeeId);

        EmployeeVitals vitals = vitalsRepository.findByEmployeeId(employeeId)
                .orElse(new EmployeeVitals());

        vitals.setEmployee(employee);
        vitals.setHeight(request.getHeight());
        vitals.setWeight(request.getWeight());
        vitals.setBloodPressure(request.getBloodPressure());
        vitals.setBloodSugar(request.getBloodSugar());
        vitals.setBloodGroup(request.getBloodGroup());

        return vitalsRepository.save(vitals);
    }

    public EmployeeVitals getVitalsByEmployee(Long employeeId) {
        employeeService.getEmployeeById(employeeId);
        return vitalsRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeVitals", employeeId));
    }
}
