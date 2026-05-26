package com.corpcare.service;

import com.corpcare.dto.ClientRequest;
import com.corpcare.entity.Appointment;
import com.corpcare.entity.Client;
import com.corpcare.entity.Employee;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.AppointmentRepository;
import com.corpcare.repository.ClientRepository;
import com.corpcare.repository.EmployeeRepository;
import com.corpcare.repository.EmployeeVitalsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeVitalsRepository vitalsRepository;
    private final AppointmentRepository appointmentRepository;

    public ClientService(ClientRepository clientRepository,
                         EmployeeRepository employeeRepository,
                         EmployeeVitalsRepository vitalsRepository,
                         AppointmentRepository appointmentRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.vitalsRepository = vitalsRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
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
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    @Transactional
    public Client updateClient(Long id, ClientRequest request) {
        Client client = getClientById(id);
        client.setCompanyName(request.getCompanyName());
        client.setContactEmail(request.getContactEmail());
        client.setContactPhone(request.getContactPhone());
        if (request.getMaxEmployees() != null) client.setMaxEmployees(request.getMaxEmployees());
        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        List<Employee> employees = employeeRepository.findByClientId(id);
        for (Employee emp : employees) {
            vitalsRepository.findByEmployeeId(emp.getId()).ifPresent(vitalsRepository::delete);
            List<Appointment> apps = appointmentRepository.findByEmployeeId(emp.getId());
            for (Appointment a : apps) {
                a.getSlot().setIsBooked(false);
                appointmentRepository.delete(a);
            }
            employeeRepository.delete(emp);
        }
        clientRepository.delete(client);
    }
}
