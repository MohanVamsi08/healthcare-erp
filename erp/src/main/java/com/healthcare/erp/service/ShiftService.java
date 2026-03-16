package com.healthcare.erp.service;

import com.healthcare.erp.dto.ShiftDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;

    public List<ShiftDTO> getByHospitalAndDate(UUID hospitalId, LocalDate date) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return shiftRepository.findByHospitalIdAndShiftDate(hospitalId, date)
                .stream().map(ShiftDTO::fromEntity).toList();
    }

    public List<ShiftDTO> getByStaff(UUID hospitalId, UUID staffId, LocalDate start, LocalDate end) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Staff does not belong to this hospital");
        }
        return shiftRepository.findByStaffIdAndShiftDateBetween(staffId, start, end)
                .stream().map(ShiftDTO::fromEntity).toList();
    }

    public ShiftDTO create(UUID hospitalId, ShiftDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Staff staff = staffRepository.findById(dto.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", dto.staffId()));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Staff does not belong to this hospital");
        }

        Shift shift = Shift.builder()
                .staff(staff)
                .hospital(hospital)
                .shiftDate(dto.shiftDate())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .shiftType(dto.shiftType())
                .status(dto.status() != null ? dto.status() : "SCHEDULED")
                .notes(dto.notes())
                .build();

        return ShiftDTO.fromEntity(shiftRepository.save(shift));
    }

    public ShiftDTO update(UUID hospitalId, UUID id, ShiftDTO dto) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", id));
        if (!shift.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Shift", id);
        }

        if (dto.shiftDate() != null) shift.setShiftDate(dto.shiftDate());
        if (dto.startTime() != null) shift.setStartTime(dto.startTime());
        if (dto.endTime() != null) shift.setEndTime(dto.endTime());
        if (dto.shiftType() != null) shift.setShiftType(dto.shiftType());
        if (dto.status() != null) shift.setStatus(dto.status());
        if (dto.notes() != null) shift.setNotes(dto.notes());

        return ShiftDTO.fromEntity(shiftRepository.save(shift));
    }
}
