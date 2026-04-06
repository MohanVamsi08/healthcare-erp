package com.healthcare.erp.service;

import com.healthcare.erp.dto.LeaveRequestDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<LeaveRequestDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return leaveRequestRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId)
                .stream().map(LeaveRequestDTO::fromEntity).toList();
    }

    public Page<LeaveRequestDTO> getByHospitalId(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return leaveRequestRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId, pageable)
                .map(LeaveRequestDTO::fromEntity);
    }

    public List<LeaveRequestDTO> getPending(UUID hospitalId) {
        return leaveRequestRepository.findByHospitalIdAndStatus(hospitalId, LeaveStatus.PENDING)
                .stream().map(LeaveRequestDTO::fromEntity).toList();
    }

    public LeaveRequestDTO create(UUID hospitalId, LeaveRequestDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Staff staff = staffRepository.findById(dto.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", dto.staffId()));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Staff does not belong to this hospital");
        }

        if (dto.endDate().isBefore(dto.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        LeaveRequest lr = LeaveRequest.builder()
                .staff(staff)
                .hospital(hospital)
                .leaveType(dto.leaveType())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .reason(dto.reason())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(lr);
        auditService.logCreate("LeaveRequest", saved.getId().toString(), hospitalId, null);
        return LeaveRequestDTO.fromEntity(saved);
    }

    public LeaveRequestDTO approve(UUID hospitalId, UUID id, UUID approvedByUserId) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        if (!lr.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("LeaveRequest", id);
        }

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Leave request is not in PENDING status");
        }

        User approver = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", approvedByUserId));

        lr.setStatus(LeaveStatus.APPROVED);
        lr.setApprovedBy(approver);
        lr.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(lr);
        auditService.log("STATUS_CHANGE", "LeaveRequest", id.toString(), hospitalId, null,
                "PENDING -> APPROVED by " + approvedByUserId);
        return LeaveRequestDTO.fromEntity(saved);
    }

    public LeaveRequestDTO reject(UUID hospitalId, UUID id) {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id));
        if (!lr.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("LeaveRequest", id);
        }

        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException("Leave request is not in PENDING status");
        }

        lr.setStatus(LeaveStatus.REJECTED);
        lr.setUpdatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(lr);
        auditService.log("STATUS_CHANGE", "LeaveRequest", id.toString(), hospitalId, null,
                "PENDING -> REJECTED");
        return LeaveRequestDTO.fromEntity(saved);
    }
}
