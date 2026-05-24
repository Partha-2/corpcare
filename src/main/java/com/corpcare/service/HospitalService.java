package com.corpcare.service;

import com.corpcare.dto.HospitalRequest;
import com.corpcare.entity.Appointment;
import com.corpcare.entity.AppointmentSlot;
import com.corpcare.entity.Hospital;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.AppointmentRepository;
import com.corpcare.repository.AppointmentSlotRepository;
import com.corpcare.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    public HospitalService(HospitalRepository hospitalRepository,
                          AppointmentSlotRepository slotRepository,
                          AppointmentRepository appointmentRepository) {
        this.hospitalRepository = hospitalRepository;
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Hospital createHospital(HospitalRequest request) {
        Hospital hospital = new Hospital(
                request.getHospitalName(),
                request.getCity(),
                request.getContactEmail()
        );
        return hospitalRepository.save(hospital);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", id));
    }

    public Hospital updateHospital(Long id, HospitalRequest request) {
        Hospital hospital = getHospitalById(id);
        hospital.setHospitalName(request.getHospitalName());
        hospital.setCity(request.getCity());
        hospital.setContactEmail(request.getContactEmail());
        return hospitalRepository.save(hospital);
    }

    @Transactional
    public void deleteHospital(Long id) {
        Hospital hospital = getHospitalById(id);
        List<AppointmentSlot> slots = slotRepository.findByHospitalId(id);
        for (AppointmentSlot slot : slots) {
            List<Appointment> apps = appointmentRepository.findBySlotHospitalId(id);
            for (Appointment a : apps) {
                if (a.getSlot().getId().equals(slot.getId())) {
                    a.getSlot().setIsBooked(false);
                    appointmentRepository.delete(a);
                }
            }
            slotRepository.delete(slot);
        }
        hospitalRepository.delete(hospital);
    }
}
