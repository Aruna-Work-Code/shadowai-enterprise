package com.aigovernance.repository;

import com.aigovernance.model.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    List<Investigation> findByTenantId(Long tenantId);
    Optional<Investigation> findByTenantIdAndId(Long tenantId, Long id);
    List<Investigation> findByTenantIdAndStatus(Long tenantId, String status);
    Optional<Investigation> findFirstByTenantIdAndToolNameAndStatusOrderByIdDesc(
            Long tenantId, String toolName, String status);
}
