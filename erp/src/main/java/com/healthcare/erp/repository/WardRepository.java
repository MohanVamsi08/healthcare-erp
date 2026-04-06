package com.healthcare.erp.repository;

import com.healthcare.erp.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID> {
    List<Ward> findByHospitalId(UUID hospitalId);
    Page<Ward> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<Ward> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    boolean existsByNameAndHospitalId(String name, UUID hospitalId);
}
