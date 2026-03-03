package com.healthcare.erp.repository;

import com.healthcare.erp.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    List<Hospital> findByStateCode(String stateCode);

    List<Hospital> findByIsActiveTrue();
}
