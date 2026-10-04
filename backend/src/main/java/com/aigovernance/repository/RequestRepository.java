package com.aigovernance.repository;

import com.aigovernance.model.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository
        extends JpaRepository<RequestEntity, Long> {

    List<RequestEntity> findByTenantId(Long tenantId);

    List<RequestEntity> findByTenantIdAndEmployeeId(
            Long tenantId,
            Long employeeId
    );

    Optional<RequestEntity> findByTenantIdAndId(
            Long tenantId,
            Long id
    );

    long countByTenantId(Long tenantId);

    List<RequestEntity> findByTenantIdAndStatus(
            Long tenantId,
            String status
    );
}