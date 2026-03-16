package com.healthcare.erp.service;

import com.healthcare.erp.dto.InvoiceDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    public List<InvoiceDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("Invoice", "LIST", hospitalId, null);
        return invoiceRepository.findByHospitalId(hospitalId)
                .stream().map(InvoiceDTO::fromEntity).toList();
    }

    public List<InvoiceDTO> getByPatient(UUID hospitalId, UUID patientId) {
        auditService.logRead("Invoice", "LIST:patient=" + patientId, hospitalId, null);
        return invoiceRepository.findByPatientIdAndHospitalId(patientId, hospitalId)
                .stream().map(InvoiceDTO::fromEntity).toList();
    }

    public InvoiceDTO getById(UUID hospitalId, UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        if (!invoice.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Invoice", invoiceId);
        }
        auditService.logRead("Invoice", invoiceId.toString(), hospitalId, null);
        return InvoiceDTO.fromEntity(invoice);
    }

    public InvoiceDTO create(UUID hospitalId, InvoiceDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }

        // Generate invoice number (hospital-specific sequential)
        long count = invoiceRepository.countByHospitalId(hospitalId);
        String invoiceNumber = String.format("INV-%04d", count + 1);

        // GST calculation
        BigDecimal subtotal = dto.subtotal();
        BigDecimal gstRate = dto.gstRate() != null ? dto.gstRate() : new BigDecimal("18.00");
        BigDecimal gstAmount = subtotal.multiply(gstRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(gstAmount);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .patient(patient)
                .hospital(hospital)
                .subtotal(subtotal)
                .gstRate(gstRate)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .dueDate(dto.dueDate())
                .notes(dto.notes())
                .build();

        // Link appointment if provided
        if (dto.appointmentId() != null) {
            Appointment appt = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", dto.appointmentId()));
            if (!appt.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("Appointment does not belong to this hospital");
            }
            invoice.setAppointment(appt);
        }

        Invoice saved = invoiceRepository.save(invoice);
        auditService.logCreate("Invoice", saved.getId().toString(), hospitalId, null);
        return InvoiceDTO.fromEntity(saved);
    }

    public InvoiceDTO issue(UUID hospitalId, UUID invoiceId) {
        Invoice invoice = getInvoiceWithTenantCheck(hospitalId, invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Only DRAFT invoices can be issued");
        }
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setUpdatedAt(java.time.LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);
        auditService.log("STATUS_CHANGE", "Invoice", invoiceId.toString(), hospitalId, null, "DRAFT -> ISSUED");
        return InvoiceDTO.fromEntity(saved);
    }

    public InvoiceDTO cancel(UUID hospitalId, UUID invoiceId) {
        Invoice invoice = getInvoiceWithTenantCheck(hospitalId, invoiceId);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("PAID invoices cannot be cancelled");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setUpdatedAt(java.time.LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);
        auditService.log("STATUS_CHANGE", "Invoice", invoiceId.toString(), hospitalId, null,
                "-> CANCELLED");
        return InvoiceDTO.fromEntity(saved);
    }

    // Package-private helper for PaymentService
    Invoice getInvoiceWithTenantCheck(UUID hospitalId, UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        if (!invoice.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Invoice", invoiceId);
        }
        return invoice;
    }
}
