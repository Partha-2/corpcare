package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.ClientRequest;
import com.corpcare.dto.EmployeeRequest;
import com.corpcare.entity.Client;
import com.corpcare.entity.Employee;
import com.corpcare.service.ClientService;
import com.corpcare.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.corpcare.config.SecurityUtil.ROLE_ADMIN;
import static com.corpcare.config.SecurityUtil.ROLE_CLIENT;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final EmployeeService employeeService;

    public ClientController(ClientService clientService, EmployeeService employeeService) {
        this.clientService = clientService;
        this.employeeService = employeeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Client>> createClient(@Valid @RequestBody ClientRequest request) {
        SecurityUtil.requireAdmin();
        Client client = clientService.createClient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client registered successfully", client));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Client>>> getAllClients() {
        SecurityUtil.requireAdmin();
        return ResponseEntity.ok(ApiResponse.success("Clients fetched successfully", clientService.getAllClients()));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Client>> getMyClient() {
        var user = SecurityUtil.requireAuthenticated();
        if (!ROLE_CLIENT.equals(user.role())) {
            throw new AccessDeniedException("Access denied");
        }
        Client client = clientService.getClientById(user.userId());
        return ResponseEntity.ok(ApiResponse.success("Client fetched successfully", client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Client>> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        SecurityUtil.requireOwnership(id, ROLE_CLIENT);
        Client client = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        SecurityUtil.requireAdmin();
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully", null));
    }

    @PostMapping("/{clientId}/employees")
    public ResponseEntity<ApiResponse<Employee>> addEmployee(
            @PathVariable Long clientId,
            @Valid @RequestBody EmployeeRequest request) {
        SecurityUtil.requireOwnership(clientId, ROLE_CLIENT);
        Employee employee = employeeService.createEmployee(clientId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee added successfully", employee));
    }

    @GetMapping("/{clientId}/employees")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployees(@PathVariable Long clientId) {
        SecurityUtil.requireOwnership(clientId, ROLE_CLIENT);
        List<Employee> employees = employeeService.getEmployeesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched successfully", employees));
    }
}
