package com.healthcare.erp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "supplier_returns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "return_number", nullable = false, unique = true)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private SupplierReturnStatus status = SupplierReturnStatus.INITIATED;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Builder.Default
    @Column(name = "initiated_at", updatable = false)
    private LocalDateTime initiatedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "supplierReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SupplierReturnItem> items = new ArrayList<>();
}
