package com.corpcare.repository;

import com.corpcare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByEmployeeId(Long employeeId);
    List<Appointment> findBySlotHospitalId(Long hospitalId);
}
