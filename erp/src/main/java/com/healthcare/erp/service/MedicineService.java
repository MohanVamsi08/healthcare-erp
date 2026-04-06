package com.healthcare.erp.service;

import com.healthcare.erp.dto.MedicineDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final HospitalRepository hospitalRepository;
    private final SupplierRepository supplierRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditService auditService;

    public List<MedicineDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return medicineRepository.findByHospitalId(hospitalId).stream()
                .map(MedicineDTO::fromEntity).toList();
    }
    public Page<MedicineDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return medicineRepository.findByHospitalId(hospitalId, pageable)
                .map(MedicineDTO::fromEntity);
    }


    public MedicineDTO getById(UUID hospitalId, UUID id) {
        return MedicineDTO.fromEntity(getMedicineWithTenantCheck(hospitalId, id));
    }

    public MedicineDTO create(UUID hospitalId, MedicineDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (dto.unitPrice() == null || dto.unitPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Unit price must be greater than zero");

        Supplier supplier = null;
        if (dto.supplierId() != null) {
            supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier", dto.supplierId()));
            if (!supplier.getHospital().getId().equals(hospitalId))
                throw new IllegalArgumentException("Supplier does not belong to this hospital");
        }

        Medicine medicine = Medicine.builder()
                .hospital(hospital).supplier(supplier)
                .name(dto.name()).genericName(dto.genericName())
                .manufacturer(dto.manufacturer()).batchNumber(dto.batchNumber())
                .expiryDate(dto.expiryDate()).dosageForm(dto.dosageForm())
                .strength(dto.strength()).unitPrice(dto.unitPrice())
                .stockQuantity(dto.stockQuantity()).reorderLevel(dto.reorderLevel())
                .build();

        Medicine saved = medicineRepository.save(medicine);
        auditService.logCreate("Medicine", saved.getId().toString(), hospitalId, null);
        return MedicineDTO.fromEntity(saved);
    }

    public MedicineDTO update(UUID hospitalId, UUID id, MedicineDTO dto) {
        Medicine medicine = getMedicineWithTenantCheck(hospitalId, id);

        if (dto.name() != null) medicine.setName(dto.name());
        if (dto.genericName() != null) medicine.setGenericName(dto.genericName());
        if (dto.manufacturer() != null) medicine.setManufacturer(dto.manufacturer());
        if (dto.batchNumber() != null) medicine.setBatchNumber(dto.batchNumber());
        if (dto.expiryDate() != null) medicine.setExpiryDate(dto.expiryDate());
        if (dto.dosageForm() != null) medicine.setDosageForm(dto.dosageForm());
        if (dto.strength() != null) medicine.setStrength(dto.strength());
        if (dto.unitPrice() != null) medicine.setUnitPrice(dto.unitPrice());
        if (dto.isActive() != null) medicine.setActive(dto.isActive());
        medicine.setUpdatedAt(LocalDateTime.now());

        Medicine saved = medicineRepository.save(medicine);
        auditService.logUpdate("Medicine", saved.getId().toString(), hospitalId, null);
        return MedicineDTO.fromEntity(saved);
    }

    public MedicineDTO addStock(UUID hospitalId, UUID id, int quantity, String notes) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Stock quantity must be positive");

        String performedBy = getAuthenticatedEmail();

        Medicine medicine = getMedicineWithTenantCheck(hospitalId, id);
        medicine.setStockQuantity(medicine.getStockQuantity() + quantity);
        medicine.setUpdatedAt(LocalDateTime.now());

        StockTransaction txn = StockTransaction.builder()
                .medicine(medicine).itemType("MEDICINE")
                .hospital(medicine.getHospital())
                .transactionType(StockTransactionType.ADJUSTMENT)
                .quantityChange(quantity).notes(notes)
                .performedBy(performedBy)
                .build();
        stockTransactionRepository.save(txn);

        Medicine saved = medicineRepository.save(medicine);
        auditService.logUpdate("Medicine", saved.getId().toString(), hospitalId, null);
        return MedicineDTO.fromEntity(saved);
    }

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    public List<MedicineDTO> getLowStock(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return medicineRepository.findByHospitalId(hospitalId).stream()
                .filter(m -> m.getStockQuantity() <= m.getReorderLevel() && m.isActive())
                .map(MedicineDTO::fromEntity).toList();
    }

    public List<MedicineDTO> getExpiring(UUID hospitalId, int daysAhead) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        LocalDate cutoff = LocalDate.now().plusDays(daysAhead);
        return medicineRepository.findByHospitalIdAndExpiryDateBefore(hospitalId, cutoff).stream()
                .filter(Medicine::isActive)
                .map(MedicineDTO::fromEntity).toList();
    }

    public Medicine getMedicineWithTenantCheck(UUID hospitalId, UUID id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", id));
        if (!medicine.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Medicine does not belong to this hospital");
        return medicine;
    }
}
