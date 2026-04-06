package com.healthcare.erp.service;

import com.healthcare.erp.dto.LabOrderDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Transactional
public class LabOrderService {

    private final LabOrderRepository labOrderRepository;
    private final LabTestRepository labTestRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<LabOrderDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("LabOrder", "LIST", hospitalId, null);
        return labOrderRepository.findByHospitalId(hospitalId).stream()
                .map(LabOrderDTO::fromEntity).toList();
    }
    public Page<LabOrderDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("LabOrder", "LIST", hospitalId, null);
        return labOrderRepository.findByHospitalId(hospitalId, pageable)
                .map(LabOrderDTO::fromEntity);
    }


    @Transactional(readOnly = true)
    public List<LabOrderDTO> getByPatient(UUID hospitalId, UUID patientId) {
        auditService.logRead("LabOrder", "LIST:patient=" + patientId, hospitalId, null);
        return labOrderRepository.findByPatientIdAndHospitalId(patientId, hospitalId).stream()
                .map(LabOrderDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public LabOrderDTO getById(UUID hospitalId, UUID orderId) {
        LabOrder order = getWithTenantCheck(hospitalId, orderId);
        auditService.logRead("LabOrder", orderId.toString(), hospitalId, null);
        return LabOrderDTO.fromEntity(order);
    }

    public LabOrderDTO create(UUID hospitalId, LabOrderDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Patient does not belong to this hospital");

        // Resolve doctor: if caller is a DOCTOR, force their own identity
        UUID resolvedDoctorId = resolveDoctor(hospitalId, dto.doctorId());
        Doctor doctor = doctorRepository.findById(resolvedDoctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", resolvedDoctorId));
        if (!doctor.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Doctor does not belong to this hospital");

        LabTest labTest = labTestRepository.findById(dto.labTestId())
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", dto.labTestId()));
        if (!labTest.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Lab test does not belong to this hospital");

        String orderNumber = "LAB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LabOrder order = LabOrder.builder()
                .orderNumber(orderNumber)
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .labTest(labTest)
                .build();

        LabOrder saved = labOrderRepository.save(order);
        auditService.logCreate("LabOrder", saved.getId().toString(), hospitalId, null);
        return LabOrderDTO.fromEntity(saved);
    }

    /**
     * Advance order through status flow: ORDERED → SAMPLE_COLLECTED → IN_PROGRESS → COMPLETED
     */
    public LabOrderDTO updateStatus(UUID hospitalId, UUID orderId, LabOrderStatus newStatus,
                                     String result, String resultNotes) {
        LabOrder order = getWithTenantCheck(hospitalId, orderId);
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if (newStatus == LabOrderStatus.COMPLETED) {
            order.setResult(result);
            order.setResultNotes(resultNotes);
            order.setCompletedAt(LocalDateTime.now());
        }
        order.setUpdatedAt(LocalDateTime.now());

        LabOrder saved = labOrderRepository.save(order);
        auditService.log("STATUS_CHANGE", "LabOrder", orderId.toString(), hospitalId, null,
                "Status changed to " + newStatus);
        return LabOrderDTO.fromEntity(saved);
    }

    public LabOrderDTO cancel(UUID hospitalId, UUID orderId) {
        LabOrder order = getWithTenantCheck(hospitalId, orderId);
        if (order.getStatus() == LabOrderStatus.COMPLETED)
            throw new IllegalArgumentException("Cannot cancel a completed lab order");

        order.setStatus(LabOrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        LabOrder saved = labOrderRepository.save(order);
        auditService.log("CANCEL", "LabOrder", orderId.toString(), hospitalId, null, null);
        return LabOrderDTO.fromEntity(saved);
    }

    private void validateStatusTransition(LabOrderStatus current, LabOrderStatus target) {
        boolean valid = switch (current) {
            case ORDERED -> target == LabOrderStatus.SAMPLE_COLLECTED;
            case SAMPLE_COLLECTED -> target == LabOrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> target == LabOrderStatus.COMPLETED;
            default -> false;
        };
        if (!valid)
            throw new IllegalArgumentException("Invalid status transition: " + current + " → " + target);
    }

    private LabOrder getWithTenantCheck(UUID hospitalId, UUID orderId) {
        LabOrder order = labOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("LabOrder", orderId));
        if (!order.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("LabOrder", orderId);
        return order;
    }

    /**
     * If the authenticated user is a DOCTOR, force their own doctor ID.
     * Admins can specify any doctor within the hospital.
     */
    private UUID resolveDoctor(UUID hospitalId, UUID requestedDoctorId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return requestedDoctorId;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_HOSPITAL_ADMIN"));
        if (isAdmin) return requestedDoctorId;

        // For DOCTOR role: resolve from authenticated user
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user is not linked to a doctor profile"));
        return doctor.getId();
    }
}
