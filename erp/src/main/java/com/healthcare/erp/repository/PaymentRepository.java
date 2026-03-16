package com.healthcare.erp.repository;

import com.healthcare.erp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByInvoiceId(UUID invoiceId);
    List<Payment> findByHospitalId(UUID hospitalId);
}
