package com.healthcare.erp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id")
    private HospitalSupply supply;

    @Column(name = "item_type", nullable = false)
    private String itemType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private StockTransactionType transactionType;

    @Column(name = "quantity_change", nullable = false)
    private int quantityChange;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
