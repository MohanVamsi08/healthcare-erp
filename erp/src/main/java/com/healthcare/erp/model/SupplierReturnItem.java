package com.healthcare.erp.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "supplier_return_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id")
    private HospitalSupply supply;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnItemReason reason;
}
