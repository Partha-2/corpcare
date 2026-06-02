package com.corpcare.config;

import com.corpcare.entity.*;
import com.corpcare.enums.BloodGroup;
import com.corpcare.enums.ShiftType;
import com.corpcare.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeVitalsRepository vitalsRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentSlotRepository slotRepository;

    public DataSeeder(ClientRepository clientRepository,
                      EmployeeRepository employeeRepository,
                      EmployeeVitalsRepository vitalsRepository,
                      HospitalRepository hospitalRepository,
                      AppointmentSlotRepository slotRepository) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.vitalsRepository = vitalsRepository;
        this.hospitalRepository = hospitalRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    public void run(String... args) {
        fixEmptyPasswords();

        if (employeeRepository.count() > 0) return;

        Client client = clientRepository.findByContactEmail("hr@vkohli.fit")
                .orElseGet(() -> clientRepository.save(
                        new Client("Virat Kohli Fitness Pvt Ltd", "hr@vkohli.fit", "+919900001111", 100)
                ));

        List<Employee> employees = employeeRepository.saveAll(List.of(
                new Employee("VK001", "Rohit Sharma", "rohit@vkohli.fit", "+919876543210", client),
                new Employee("VK002", "Rahul Dravid", "rahul@vkohli.fit", "+919876543211", client)
        ));

        vitalsRepository.save(new EmployeeVitals(employees.get(0), 175.0, 72.0, "120/80", 95.0, BloodGroup.O_POSITIVE));
        vitalsRepository.save(new EmployeeVitals(employees.get(1), 168.0, 68.0, "118/76", 88.0, BloodGroup.B_POSITIVE));

        Hospital hospital = hospitalRepository.save(
                new Hospital("Apollo Bengaluru", "Bengaluru", "contact@apollo.in")
        );

        slotRepository.save(new AppointmentSlot(hospital, LocalDate.now().plusDays(1), ShiftType.MORNING_8_TO_4));
        slotRepository.save(new AppointmentSlot(hospital, LocalDate.now().plusDays(1), ShiftType.EVENING_4_TO_12));
        slotRepository.save(new AppointmentSlot(hospital, LocalDate.now().plusDays(2), ShiftType.MORNING_8_TO_4));
        slotRepository.save(new AppointmentSlot(hospital, LocalDate.now().plusDays(2), ShiftType.NIGHT_12_TO_8));
    }

    private void fixEmptyPasswords() {
        for (Client c : clientRepository.findAll()) {
            if (c.getPassword() == null || c.getPassword().isEmpty()) {
                c.setPassword("client123");
                clientRepository.save(c);
            }
        }
        for (Hospital h : hospitalRepository.findAll()) {
            if (h.getPassword() == null || h.getPassword().isEmpty()) {
                h.setPassword("hospital123");
                hospitalRepository.save(h);
            }
        }
    }
}
