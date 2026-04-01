package com.healthcare.erp.repository;

import com.healthcare.erp.model.Invoice;
import com.healthcare.erp.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByHospitalId(UUID hospitalId);
    List<Invoice> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    List<Invoice> findByHospitalIdAndStatus(UUID hospitalId, InvoiceStatus status);
    long countByHospitalId(UUID hospitalId);
    long countByHospitalIdAndStatus(UUID hospitalId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.hospital.id = :hospitalId")
    BigDecimal sumTotalAmountByHospitalId(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT COALESCE(SUM(i.gstAmount), 0) FROM Invoice i WHERE i.hospital.id = :hospitalId")
    BigDecimal sumGstAmountByHospitalId(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT COALESCE(SUM(i.paidAmount), 0) FROM Invoice i WHERE i.hospital.id = :hospitalId")
    BigDecimal sumPaidAmountByHospitalId(@Param("hospitalId") UUID hospitalId);

    @Query(value = "SELECT MAX(CAST(SUBSTRING(invoice_number, 5) AS INTEGER)) FROM invoices WHERE hospital_id = ?1",
            nativeQuery = true)
    Optional<Integer> findMaxInvoiceSequenceByHospitalId(UUID hospitalId);
}
