package com.aigovernance.repository;

import com.aigovernance.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditRepository extends JpaRepository<AuditEvent, Long> {
    Optional<AuditEvent> findTopByTenantIdOrderByIdDesc(Long tenantId);
    List<AuditEvent> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<AuditEvent> findByEntityTypeAndEntityId(String type, Long id);
    List<AuditEvent> findByTenantIdAndEntityTypeAndEntityId(Long tenantId, String type, Long id);
    long countByTenantId(Long tenantId);
}
