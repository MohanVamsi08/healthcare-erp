package com.healthcare.erp.service;

import com.healthcare.erp.dto.AppointmentDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentDTO create(UUID hospitalId, AppointmentDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.doctorId()));

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .appointmentDateTime(dto.appointmentDateTime())
                .reason(dto.reason())
                .notes(dto.notes())
                .build();

        return AppointmentDTO.fromEntity(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return appointmentRepository.findByHospitalId(hospitalId)
                .stream().map(AppointmentDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getById(UUID hospitalId, UUID appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        if (!appt.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }
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

        return AppointmentDTO.fromEntity(appointmentRepository.save(appt));
    }

    public AppointmentDTO updateStatus(UUID hospitalId, UUID appointmentId, AppointmentStatus status) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        if (!appt.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Appointment", appointmentId);
        }

        appt.setStatus(status);
        appt.setUpdatedAt(java.time.LocalDateTime.now());

        return AppointmentDTO.fromEntity(appointmentRepository.save(appt));
    }
}
