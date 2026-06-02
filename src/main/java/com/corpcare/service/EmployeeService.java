package com.corpcare.service;

import com.corpcare.dto.EmployeeRequest;
import com.corpcare.entity.Client;
import com.corpcare.entity.Employee;
import com.corpcare.exception.BusinessException;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ClientService clientService;

    public EmployeeService(EmployeeRepository employeeRepository, ClientService clientService) {
        this.employeeRepository = employeeRepository;
        this.clientService = clientService;
    }

    @Transactional
    public Employee createEmployee(Long clientId, EmployeeRequest request) {
        Client client = clientService.getClientById(clientId);

        long currentCount = employeeRepository.countByClientId(clientId);
        if (currentCount >= client.getMaxEmployees()) {
            throw new BusinessException(
                "Client has reached maximum employee limit of " + client.getMaxEmployees()
            );
        }

        Employee employee = new Employee(
                request.getEmployeeCode(),
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                client
        );
        return employeeRepository.save(employee);
    }

    public List<Employee> getEmployeesByClient(Long clientId) {
        clientService.getClientById(clientId); // ensure client exists
        return employeeRepository.findByClientId(clientId);
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    public Employee getByEmployeeCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BusinessException("Employee not found with code: " + employeeCode));
    }

    public Employee verifyEmployee(String email, String employeeCode) {
        return employeeRepository.findByEmailAndEmployeeCode(email, employeeCode)
                .orElseThrow(() -> new BusinessException("Invalid email or employee code"));
    }
}
