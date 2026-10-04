package com.aigovernance.repository;

import com.aigovernance.model.AiTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiToolRepository extends JpaRepository<AiTool, Long> {
    List<AiTool> findByTenantId(Long tenantId);
    Optional<AiTool> findByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}
