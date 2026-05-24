package com.corpcare.service;

import com.corpcare.dto.SlotRequest;
import com.corpcare.entity.AppointmentSlot;
import com.corpcare.entity.Hospital;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.AppointmentSlotRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentSlotService {

    private final AppointmentSlotRepository slotRepository;
    private final HospitalService hospitalService;

    public AppointmentSlotService(AppointmentSlotRepository slotRepository, HospitalService hospitalService) {
        this.slotRepository = slotRepository;
        this.hospitalService = hospitalService;
    }

    public AppointmentSlot createSlot(Long hospitalId, SlotRequest request) {
        Hospital hospital = hospitalService.getHospitalById(hospitalId);
        AppointmentSlot slot = new AppointmentSlot(hospital, request.getSlotDate(), request.getShiftType());
        return slotRepository.save(slot);
    }

    public List<AppointmentSlot> getAvailableSlots(Long hospitalId, LocalDate date) {
        hospitalService.getHospitalById(hospitalId);
        if (date != null) {
            return slotRepository.findByHospitalIdAndSlotDateAndIsBookedFalse(hospitalId, date);
        }
        return slotRepository.findByHospitalIdAndIsBookedFalse(hospitalId);
    }

    public List<AppointmentSlot> getAllSlotsByHospital(Long hospitalId) {
        hospitalService.getHospitalById(hospitalId);
        return slotRepository.findByHospitalId(hospitalId);
    }

    public AppointmentSlot getSlotById(Long id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppointmentSlot", id));
    }
}
