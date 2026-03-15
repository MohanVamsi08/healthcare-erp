package com.healthcare.erp.service;

import com.healthcare.erp.dto.LeaveRequestDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;

    public List<LeaveRequestDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return leaveRequestRepository.findByHospitalIdOrderByCreatedAtDesc(hospitalId)
                .stream().map(LeaveRequestDTO::fromEntity).toList();
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

        return LeaveRequestDTO.fromEntity(leaveRequestRepository.save(lr));
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

        return LeaveRequestDTO.fromEntity(leaveRequestRepository.save(lr));
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

        return LeaveRequestDTO.fromEntity(leaveRequestRepository.save(lr));
    }
}
