package com.healthcare.erp.service;

import com.healthcare.erp.dto.StockTransactionDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransactionService {

    private final StockTransactionRepository txnRepository;
    private final HospitalRepository hospitalRepository;

    public List<StockTransactionDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return txnRepository.findByHospitalId(hospitalId).stream()
                .map(StockTransactionDTO::fromEntity).toList();
    }

    public List<StockTransactionDTO> getByMedicine(UUID medicineId) {
        return txnRepository.findByMedicineId(medicineId).stream()
                .map(StockTransactionDTO::fromEntity).toList();
    }

    public List<StockTransactionDTO> getBySupply(UUID supplyId) {
        return txnRepository.findBySupplyId(supplyId).stream()
                .map(StockTransactionDTO::fromEntity).toList();
    }
}
