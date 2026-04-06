package com.healthcare.erp.service;

import com.healthcare.erp.dto.HospitalSupplyDTO;
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
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalSupplyService {

    private final HospitalSupplyRepository supplyRepository;
    private final HospitalRepository hospitalRepository;
    private final SupplierRepository supplierRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditService auditService;

    public List<HospitalSupplyDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return supplyRepository.findByHospitalId(hospitalId).stream()
                .map(HospitalSupplyDTO::fromEntity).toList();
    }
    public Page<HospitalSupplyDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return supplyRepository.findByHospitalId(hospitalId, pageable)
                .map(HospitalSupplyDTO::fromEntity);
    }


    public HospitalSupplyDTO getById(UUID hospitalId, UUID id) {
        return HospitalSupplyDTO.fromEntity(getSupplyWithTenantCheck(hospitalId, id));
    }

    public HospitalSupplyDTO create(UUID hospitalId, HospitalSupplyDTO dto) {
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

        HospitalSupply supply = HospitalSupply.builder()
                .hospital(hospital).supplier(supplier)
                .name(dto.name()).category(dto.category())
                .manufacturer(dto.manufacturer()).batchNumber(dto.batchNumber())
                .expiryDate(dto.expiryDate()).unit(dto.unit())
                .unitPrice(dto.unitPrice())
                .stockQuantity(dto.stockQuantity()).reorderLevel(dto.reorderLevel())
                .build();

        HospitalSupply saved = supplyRepository.save(supply);
        auditService.logCreate("HospitalSupply", saved.getId().toString(), hospitalId, null);
        return HospitalSupplyDTO.fromEntity(saved);
    }

    public HospitalSupplyDTO update(UUID hospitalId, UUID id, HospitalSupplyDTO dto) {
        HospitalSupply supply = getSupplyWithTenantCheck(hospitalId, id);

        if (dto.name() != null) supply.setName(dto.name());
        if (dto.category() != null) supply.setCategory(dto.category());
        if (dto.manufacturer() != null) supply.setManufacturer(dto.manufacturer());
        if (dto.batchNumber() != null) supply.setBatchNumber(dto.batchNumber());
        if (dto.expiryDate() != null) supply.setExpiryDate(dto.expiryDate());
        if (dto.unit() != null) supply.setUnit(dto.unit());
        if (dto.unitPrice() != null) supply.setUnitPrice(dto.unitPrice());
        if (dto.isActive() != null) supply.setActive(dto.isActive());
        supply.setUpdatedAt(LocalDateTime.now());

        HospitalSupply saved = supplyRepository.save(supply);
        auditService.logUpdate("HospitalSupply", saved.getId().toString(), hospitalId, null);
        return HospitalSupplyDTO.fromEntity(saved);
    }

    public HospitalSupplyDTO addStock(UUID hospitalId, UUID id, int quantity, String notes) {
        if (quantity <= 0)
            throw new IllegalArgumentException("Stock quantity must be positive");

        String performedBy = getAuthenticatedEmail();

        HospitalSupply supply = getSupplyWithTenantCheck(hospitalId, id);
        supply.setStockQuantity(supply.getStockQuantity() + quantity);
        supply.setUpdatedAt(LocalDateTime.now());

        StockTransaction txn = StockTransaction.builder()
                .supply(supply).itemType("SUPPLY")
                .hospital(supply.getHospital())
                .transactionType(StockTransactionType.ADJUSTMENT)
                .quantityChange(quantity).notes(notes)
                .performedBy(performedBy)
                .build();
        stockTransactionRepository.save(txn);

        HospitalSupply saved = supplyRepository.save(supply);
        auditService.logUpdate("HospitalSupply", saved.getId().toString(), hospitalId, null);
        return HospitalSupplyDTO.fromEntity(saved);
    }

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    public List<HospitalSupplyDTO> getLowStock(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return supplyRepository.findByHospitalId(hospitalId).stream()
                .filter(s -> s.getStockQuantity() <= s.getReorderLevel() && s.isActive())
                .map(HospitalSupplyDTO::fromEntity).toList();
    }

    public HospitalSupply getSupplyWithTenantCheck(UUID hospitalId, UUID id) {
        HospitalSupply supply = supplyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HospitalSupply", id));
        if (!supply.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Supply does not belong to this hospital");
        return supply;
    }
}
