package com.healthcare.erp.service;

import com.healthcare.erp.dto.SupplierDTO;
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

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    public List<SupplierDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return supplierRepository.findByHospitalId(hospitalId).stream()
                .map(SupplierDTO::fromEntity).toList();
    }

    public SupplierDTO getById(UUID hospitalId, UUID id) {
        Supplier supplier = getSupplierWithTenantCheck(hospitalId, id);
        return SupplierDTO.fromEntity(supplier);
    }

    public SupplierDTO create(UUID hospitalId, SupplierDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (dto.name() == null || dto.name().isBlank())
            throw new IllegalArgumentException("Supplier name is required");

        Supplier supplier = Supplier.builder()
                .hospital(hospital)
                .name(dto.name())
                .contactPerson(dto.contactPerson())
                .email(dto.email())
                .phone(dto.phone())
                .address(dto.address())
                .gstNumber(dto.gstNumber())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        auditService.logCreate("Supplier", saved.getId().toString(), hospitalId, null);
        return SupplierDTO.fromEntity(saved);
    }

    public SupplierDTO update(UUID hospitalId, UUID id, SupplierDTO dto) {
        Supplier supplier = getSupplierWithTenantCheck(hospitalId, id);

        if (dto.name() != null) supplier.setName(dto.name());
        if (dto.contactPerson() != null) supplier.setContactPerson(dto.contactPerson());
        if (dto.email() != null) supplier.setEmail(dto.email());
        if (dto.phone() != null) supplier.setPhone(dto.phone());
        if (dto.address() != null) supplier.setAddress(dto.address());
        if (dto.gstNumber() != null) supplier.setGstNumber(dto.gstNumber());
        if (dto.isActive() != null) supplier.setActive(dto.isActive());
        supplier.setUpdatedAt(LocalDateTime.now());

        Supplier saved = supplierRepository.save(supplier);
        auditService.logUpdate("Supplier", saved.getId().toString(), hospitalId, null);
        return SupplierDTO.fromEntity(saved);
    }

    public Supplier getSupplierWithTenantCheck(UUID hospitalId, UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        if (!supplier.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Supplier does not belong to this hospital");
        return supplier;
    }
}
