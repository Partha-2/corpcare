package com.corpcare.service;

import com.corpcare.dto.ClientRequest;
import com.corpcare.entity.Client;
import com.corpcare.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client createClient(ClientRequest request) {
        Client client = new Client(
                request.getCompanyName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getMaxEmployees() != null ? request.getMaxEmployees() : 100
        );
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new com.corpcare.exception.ResourceNotFoundException("Client", id));
    }
}
