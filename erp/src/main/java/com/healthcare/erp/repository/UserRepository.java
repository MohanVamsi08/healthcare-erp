package com.healthcare.erp.repository;

import com.healthcare.erp.model.User;
import com.healthcare.erp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByHospitalIdAndIsActiveTrue(UUID hospitalId);

    List<User> findByHospitalIdAndRole(UUID hospitalId, Role role);
}
