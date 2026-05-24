package com.corpcare.repository;

import com.corpcare.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    List<AppointmentSlot> findByHospitalIdAndIsBookedFalse(Long hospitalId);
    List<AppointmentSlot> findByHospitalIdAndSlotDateAndIsBookedFalse(Long hospitalId, LocalDate slotDate);
    List<AppointmentSlot> findByHospitalId(Long hospitalId);
}
