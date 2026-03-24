package com.healthcare.erp.service;

import com.healthcare.erp.dto.PurchaseOrderDTO;
import com.healthcare.erp.dto.PurchaseOrderItemDTO;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final HospitalRepository hospitalRepository;
    private final SupplierService supplierService;
    private final MedicineService medicineService;
    private final HospitalSupplyService supplyService;
    private final MedicineRepository medicineRepository;
    private final HospitalSupplyRepository supplyRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditService auditService;

    public List<PurchaseOrderDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return poRepository.findByHospitalId(hospitalId).stream()
                .map(PurchaseOrderDTO::fromEntity).toList();
    }

    public PurchaseOrderDTO getById(UUID hospitalId, UUID id) {
        return PurchaseOrderDTO.fromEntity(getPOWithTenantCheck(hospitalId, id));
    }

    public PurchaseOrderDTO create(UUID hospitalId, PurchaseOrderDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Supplier supplier = supplierService.getSupplierWithTenantCheck(hospitalId, dto.supplierId());

        if (dto.items() == null || dto.items().isEmpty())
            throw new IllegalArgumentException("At least one item is required");

        String orderNumber = "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PurchaseOrder po = PurchaseOrder.builder()
                .orderNumber(orderNumber)
                .hospital(hospital).supplier(supplier)
                .notes(dto.notes())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO itemDto : dto.items()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(po);
            item.setItemType(itemDto.itemType());
            item.setQuantity(itemDto.quantity());
            item.setUnitCost(itemDto.unitCost());

            if ("MEDICINE".equals(itemDto.itemType())) {
                Medicine med = medicineService.getMedicineWithTenantCheck(hospitalId, itemDto.medicineId());
                item.setMedicine(med);
            } else if ("SUPPLY".equals(itemDto.itemType())) {
                HospitalSupply sup = supplyService.getSupplyWithTenantCheck(hospitalId, itemDto.supplyId());
                item.setSupply(sup);
            } else {
                throw new IllegalArgumentException("Invalid item type: " + itemDto.itemType());
            }

            total = total.add(itemDto.unitCost().multiply(BigDecimal.valueOf(itemDto.quantity())));
            po.getItems().add(item);
        }
        po.setTotalAmount(total);

        PurchaseOrder saved = poRepository.save(po);
        auditService.logCreate("PurchaseOrder", saved.getId().toString(), hospitalId, null);
        return PurchaseOrderDTO.fromEntity(saved);
    }

    public PurchaseOrderDTO markDelivered(UUID hospitalId, UUID id,
                                          String deliveredBy, String deliveryLocation) {
        PurchaseOrder po = getPOWithTenantCheck(hospitalId, id);
        if (po.getStatus() != PurchaseOrderStatus.ORDERED)
            throw new IllegalArgumentException("Only ORDERED purchase orders can be marked as delivered");

        po.setStatus(PurchaseOrderStatus.DELIVERED);
        po.setDeliveredBy(deliveredBy);
        po.setDeliveryLocation(deliveryLocation);
        po.setDeliveredAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = poRepository.save(po);
        auditService.logUpdate("PurchaseOrder", saved.getId().toString(), hospitalId, null);
        return PurchaseOrderDTO.fromEntity(saved);
    }

    public PurchaseOrderDTO receive(UUID hospitalId, UUID id, String receivedBy) {
        PurchaseOrder po = getPOWithTenantCheck(hospitalId, id);
        if (po.getStatus() != PurchaseOrderStatus.DELIVERED)
            throw new IllegalArgumentException("Only DELIVERED purchase orders can be received");

        po.setStatus(PurchaseOrderStatus.RECEIVED);
        po.setReceivedBy(receivedBy);
        po.setReceivedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());

        // Auto-increment stock for each item
        for (PurchaseOrderItem item : po.getItems()) {
            StockTransaction txn = StockTransaction.builder()
                    .hospital(po.getHospital())
                    .transactionType(StockTransactionType.PURCHASE)
                    .quantityChange(item.getQuantity())
                    .referenceId(po.getId())
                    .performedBy(getAuthenticatedEmail())
                    .notes("PO " + po.getOrderNumber() + " received")
                    .build();

            if ("MEDICINE".equals(item.getItemType())) {
                Medicine med = item.getMedicine();
                med.setStockQuantity(med.getStockQuantity() + item.getQuantity());
                med.setUpdatedAt(LocalDateTime.now());
                medicineRepository.save(med);
                txn.setMedicine(med);
                txn.setItemType("MEDICINE");
            } else {
                HospitalSupply sup = item.getSupply();
                sup.setStockQuantity(sup.getStockQuantity() + item.getQuantity());
                sup.setUpdatedAt(LocalDateTime.now());
                supplyRepository.save(sup);
                txn.setSupply(sup);
                txn.setItemType("SUPPLY");
            }
            stockTransactionRepository.save(txn);
        }

        PurchaseOrder saved = poRepository.save(po);
        auditService.logUpdate("PurchaseOrder", saved.getId().toString(), hospitalId, null);
        return PurchaseOrderDTO.fromEntity(saved);
    }

    public PurchaseOrderDTO cancel(UUID hospitalId, UUID id) {
        PurchaseOrder po = getPOWithTenantCheck(hospitalId, id);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED)
            throw new IllegalArgumentException("Cannot cancel a received purchase order");

        po.setStatus(PurchaseOrderStatus.CANCELLED);
        po.setUpdatedAt(LocalDateTime.now());

        PurchaseOrder saved = poRepository.save(po);
        auditService.logUpdate("PurchaseOrder", saved.getId().toString(), hospitalId, null);
        return PurchaseOrderDTO.fromEntity(saved);
    }

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private PurchaseOrder getPOWithTenantCheck(UUID hospitalId, UUID id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (!po.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Purchase order does not belong to this hospital");
        return po;
    }
}
