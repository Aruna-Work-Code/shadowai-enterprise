package com.aigovernance.repository;

import com.aigovernance.model.TenantMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantMembershipRepository
        extends JpaRepository<TenantMembership, Long> {

    List<TenantMembership> findByTenantId(Long tenantId);

    Optional<TenantMembership> findFirstByUserId(Long userId);

    Optional<TenantMembership> findByTenantIdAndUserId(
            Long tenantId,
            Long userId);
}
