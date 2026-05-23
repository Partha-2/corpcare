package com.corpcare.repository;

import com.corpcare.entity.AppointmentSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {
    List<AppointmentSlot> findByHospitalIdAndIsBookedFalse(Long hospitalId);
    List<AppointmentSlot> findByHospitalId(Long hospitalId);
}
