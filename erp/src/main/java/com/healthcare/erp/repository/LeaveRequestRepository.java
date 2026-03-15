package com.healthcare.erp.repository;

import com.healthcare.erp.model.LeaveRequest;
import com.healthcare.erp.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByHospitalIdAndStatus(UUID hospitalId, LeaveStatus status);
    List<LeaveRequest> findByStaffIdOrderByCreatedAtDesc(UUID staffId);
    List<LeaveRequest> findByHospitalIdOrderByCreatedAtDesc(UUID hospitalId);
}
