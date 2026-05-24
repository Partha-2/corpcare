package com.corpcare.controller;

import com.corpcare.config.SecurityUtil;
import com.corpcare.dto.ApiResponse;
import com.corpcare.dto.ClientRequest;
import com.corpcare.entity.Client;
import com.corpcare.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Client>> createClient(@Valid @RequestBody ClientRequest request) {
        SecurityUtil.requireOwnership(0L, "ADMIN");
        Client client = clientService.createClient(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client registered successfully", client));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Client>>> getAllClients() {
        if (SecurityUtil.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.success("Clients fetched successfully", clientService.getAllClients()));
        }
        if (SecurityUtil.isClient()) {
            var user = SecurityUtil.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success("Clients fetched successfully",
                    List.of(clientService.getClientById(user.userId()))));
        }
        throw new org.springframework.security.access.AccessDeniedException("Access denied");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Client>> updateClient(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        SecurityUtil.requireOwnership(id, "CLIENT");
        Client client = clientService.updateClient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", client));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        SecurityUtil.requireOwnership(0L, "ADMIN");
        clientService.deleteClient(id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully", null));
    }
}
