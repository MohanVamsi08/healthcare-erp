package com.healthcare.erp.service;

import com.healthcare.erp.dto.AppointmentDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AuditService auditService;

    public AppointmentDTO create(UUID hospitalId, AppointmentDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.doctorId()));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Doctor does not belong to this hospital");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .appointmentDateTime(dto.appointmentDateTime())
                .reason(dto.reason())
                .notes(dto.notes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        auditService.logCreate("Appointment", saved.getId().toString(), hospitalId, null);
        return AppointmentDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("Appointment", "LIST", hospitalId, null);
        return appointmentRepository.findByHospitalId(hospitalId)
                .stream().map(AppointmentDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("Appointment", "LIST", hospitalId, null);
        return appointmentRepository.findByHospitalId(hospitalId, pageable)
                .map(AppointmentDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getById(UUID hospitalId, UUID appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        if (!appt.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }
        auditService.logRead("Appointment", appointmentId.toString(), hospitalId, null);
        return AppointmentDTO.fromEntity(appt);
    }

    public AppointmentDTO update(UUID hospitalId, UUID appointmentId, AppointmentDTO dto) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        if (!appt.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        if (dto.appointmentDateTime() != null) appt.setAppointmentDateTime(dto.appointmentDateTime());
        if (dto.reason() != null) appt.setReason(dto.reason());
        if (dto.notes() != null) appt.setNotes(dto.notes());
        appt.setUpdatedAt(java.time.LocalDateTime.now());

        Appointment saved = appointmentRepository.save(appt);
        auditService.log("UPDATE", "Appointment", appointmentId.toString(), hospitalId, null, null);
        return AppointmentDTO.fromEntity(saved);
    }

    public AppointmentDTO updateStatus(UUID hospitalId, UUID appointmentId, AppointmentStatus status) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        if (!appt.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        String previousStatus = appt.getStatus().name();
        appt.setStatus(status);
        appt.setUpdatedAt(java.time.LocalDateTime.now());
        Appointment saved = appointmentRepository.save(appt);
        auditService.log("STATUS_CHANGE", "Appointment", appointmentId.toString(), hospitalId, null,
                previousStatus + " -> " + status.name());
        return AppointmentDTO.fromEntity(saved);
    }
}
