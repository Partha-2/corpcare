package com.corpcare.service;

import com.corpcare.dto.HospitalRequest;
import com.corpcare.entity.Hospital;
import com.corpcare.exception.ResourceNotFoundException;
import com.corpcare.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
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
}
