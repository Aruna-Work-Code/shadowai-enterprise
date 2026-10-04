package com.aigovernance.repository;

import com.aigovernance.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByKey(String key);
    boolean existsByKey(String key);
}
