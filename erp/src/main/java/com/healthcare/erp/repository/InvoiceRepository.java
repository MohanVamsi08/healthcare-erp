package com.healthcare.erp.repository;

import com.healthcare.erp.model.Invoice;
import com.healthcare.erp.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByHospitalId(UUID hospitalId);
    List<Invoice> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    List<Invoice> findByHospitalIdAndStatus(UUID hospitalId, InvoiceStatus status);
    long countByHospitalId(UUID hospitalId);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(invoice_number, 5) AS INTEGER)) FROM invoices WHERE hospital_id = ?1",
            nativeQuery = true)
    Optional<Integer> findMaxInvoiceSequenceByHospitalId(UUID hospitalId);
}
