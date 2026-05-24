package com.corpcare.service;

import com.corpcare.dto.AppointmentRequest;
import com.corpcare.entity.Appointment;
import com.corpcare.entity.AppointmentSlot;
import com.corpcare.entity.Employee;
import com.corpcare.exception.BusinessException;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.AppointmentRepository;
import com.corpcare.repository.AppointmentSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final EmployeeService employeeService;
    private final AppointmentSlotService slotService;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              AppointmentSlotRepository slotRepository,
                              EmployeeService employeeService,
                              AppointmentSlotService slotService,
                              NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.employeeService = employeeService;
        this.slotService = slotService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Appointment bookAppointment(AppointmentRequest request) {
        Employee employee = employeeService.getEmployeeById(request.getEmployeeId());
        AppointmentSlot slot = slotService.getSlotById(request.getSlotId());

        if (slot.getIsBooked()) {
            throw new BusinessException("Slot already booked");
        }

        List<Appointment> existing = appointmentRepository.findByEmployeeId(employee.getId());
        if (!existing.isEmpty()) {
            throw new BusinessException("Employee already has an appointment booked");
        }

        slot.setIsBooked(true);
        slotRepository.save(slot);

        Appointment appointment = new Appointment(employee, slot);
        appointment = appointmentRepository.save(appointment);

        notificationService.notifyAppointmentBooked(appointment);

        return appointment;
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    public List<Appointment> getAppointmentsByEmployee(Long employeeId) {
        return appointmentRepository.findByEmployeeId(employeeId);
    }

    public List<Appointment> getAppointmentsByHospital(Long hospitalId) {
        return appointmentRepository.findBySlotHospitalId(hospitalId);
    }

    public List<Appointment> getAppointmentsByClient(Long clientId) {
        return appointmentRepository.findByEmployeeClientId(clientId);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        AppointmentSlot slot = appointment.getSlot();
        slot.setIsBooked(false);
        slotRepository.save(slot);

        appointmentRepository.delete(appointment);
    }
}
