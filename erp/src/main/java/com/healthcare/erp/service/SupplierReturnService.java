package com.healthcare.erp.service;

import com.healthcare.erp.dto.SupplierReturnDTO;
import com.healthcare.erp.dto.SupplierReturnItemDTO;
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
public class SupplierReturnService {

    private final SupplierReturnRepository returnRepository;
    private final HospitalRepository hospitalRepository;
    private final SupplierService supplierService;
    private final MedicineService medicineService;
    private final HospitalSupplyService supplyService;
    private final MedicineRepository medicineRepository;
    private final HospitalSupplyRepository supplyRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditService auditService;

    public List<SupplierReturnDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return returnRepository.findByHospitalId(hospitalId).stream()
                .map(SupplierReturnDTO::fromEntity).toList();
    }

    public SupplierReturnDTO getById(UUID hospitalId, UUID id) {
        return SupplierReturnDTO.fromEntity(getReturnWithTenantCheck(hospitalId, id));
    }

    public SupplierReturnDTO create(UUID hospitalId, SupplierReturnDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Supplier supplier = supplierService.getSupplierWithTenantCheck(hospitalId, dto.supplierId());

        if (dto.items() == null || dto.items().isEmpty())
            throw new IllegalArgumentException("At least one return item is required");
        if (dto.reason() == null || dto.reason().isBlank())
            throw new IllegalArgumentException("Return reason is required");

        String returnNumber = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        SupplierReturn ret = SupplierReturn.builder()
                .returnNumber(returnNumber)
                .hospital(hospital).supplier(supplier)
                .reason(dto.reason())
                .build();

        for (SupplierReturnItemDTO itemDto : dto.items()) {
            SupplierReturnItem item = new SupplierReturnItem();
            item.setSupplierReturn(ret);
            item.setItemType(itemDto.itemType());
            item.setQuantity(itemDto.quantity());
            item.setReason(itemDto.reason());

            if ("MEDICINE".equals(itemDto.itemType())) {
                Medicine med = medicineService.getMedicineWithTenantCheck(hospitalId, itemDto.medicineId());
                item.setMedicine(med);
            } else if ("SUPPLY".equals(itemDto.itemType())) {
                HospitalSupply sup = supplyService.getSupplyWithTenantCheck(hospitalId, itemDto.supplyId());
                item.setSupply(sup);
            } else {
                throw new IllegalArgumentException("Invalid item type: " + itemDto.itemType());
            }
            ret.getItems().add(item);
        }

        SupplierReturn saved = returnRepository.save(ret);
        auditService.logCreate("SupplierReturn", saved.getId().toString(), hospitalId, null);
        return SupplierReturnDTO.fromEntity(saved);
    }

    public SupplierReturnDTO markShipped(UUID hospitalId, UUID id) {
        SupplierReturn ret = getReturnWithTenantCheck(hospitalId, id);
        if (ret.getStatus() != SupplierReturnStatus.INITIATED)
            throw new IllegalArgumentException("Only INITIATED returns can be shipped");

        ret.setStatus(SupplierReturnStatus.SHIPPED);
        ret.setUpdatedAt(LocalDateTime.now());

        SupplierReturn saved = returnRepository.save(ret);
        return SupplierReturnDTO.fromEntity(saved);
    }

    public SupplierReturnDTO complete(UUID hospitalId, UUID id) {
        SupplierReturn ret = getReturnWithTenantCheck(hospitalId, id);
        if (ret.getStatus() != SupplierReturnStatus.SHIPPED)
            throw new IllegalArgumentException("Only SHIPPED returns can be completed");

        // Decrement stock for returned items
        for (SupplierReturnItem item : ret.getItems()) {
            StockTransaction txn = StockTransaction.builder()
                    .hospital(ret.getHospital())
                    .transactionType(StockTransactionType.RETURN)
                    .quantityChange(-item.getQuantity())
                    .referenceId(ret.getId())
                    .notes("Return " + ret.getReturnNumber() + " completed")
                    .build();

            if ("MEDICINE".equals(item.getItemType())) {
                Medicine med = item.getMedicine();
                med.setStockQuantity(med.getStockQuantity() - item.getQuantity());
                med.setUpdatedAt(LocalDateTime.now());
                medicineRepository.save(med);
                txn.setMedicine(med);
                txn.setItemType("MEDICINE");
            } else {
                HospitalSupply sup = item.getSupply();
                sup.setStockQuantity(sup.getStockQuantity() - item.getQuantity());
                sup.setUpdatedAt(LocalDateTime.now());
                supplyRepository.save(sup);
                txn.setSupply(sup);
                txn.setItemType("SUPPLY");
            }
            stockTransactionRepository.save(txn);
        }

        ret.setStatus(SupplierReturnStatus.COMPLETED);
        ret.setCompletedAt(LocalDateTime.now());
        ret.setUpdatedAt(LocalDateTime.now());

        SupplierReturn saved = returnRepository.save(ret);
        auditService.logUpdate("SupplierReturn", saved.getId().toString(), hospitalId, null);
        return SupplierReturnDTO.fromEntity(saved);
    }

    private SupplierReturn getReturnWithTenantCheck(UUID hospitalId, UUID id) {
        SupplierReturn ret = returnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupplierReturn", id));
        if (!ret.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Return does not belong to this hospital");
        return ret;
    }
}
