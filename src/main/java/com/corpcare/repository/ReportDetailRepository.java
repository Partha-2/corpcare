package com.corpcare.repository;

import com.corpcare.entity.ReportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportDetailRepository extends JpaRepository<ReportDetail, Long> {

    List<ReportDetail> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    @Query("SELECT r FROM ReportDetail r WHERE r.haemoglobinStatus = 'HIGH' OR r.haemoglobinStatus = 'LOW' " +
           "OR r.rbcCountStatus = 'HIGH' OR r.rbcCountStatus = 'LOW' " +
           "OR r.totalWbcCountStatus = 'HIGH' OR r.totalWbcCountStatus = 'LOW' " +
           "OR r.plateletCountStatus = 'HIGH' OR r.plateletCountStatus = 'LOW' " +
           "OR r.esrStatus = 'HIGH' " +
           "ORDER BY r.createdAt DESC")
    List<ReportDetail> findCriticalReports();

    List<ReportDetail> findByHaemoglobinBetween(Double min, Double max);
    List<ReportDetail> findByRbcCountBetween(Double min, Double max);
    List<ReportDetail> findByTotalWbcCountBetween(Double min, Double max);
    List<ReportDetail> findByEsrGreaterThan(Double max);
}
