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

    public List<StockTransactionDTO> getByMedicine(UUID hospitalId, UUID medicineId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return txnRepository.findByMedicineIdAndHospitalId(medicineId, hospitalId).stream()
                .map(StockTransactionDTO::fromEntity).toList();
    }

    public List<StockTransactionDTO> getBySupply(UUID hospitalId, UUID supplyId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        return txnRepository.findBySupplyIdAndHospitalId(supplyId, hospitalId).stream()
                .map(StockTransactionDTO::fromEntity).toList();
    }
}
