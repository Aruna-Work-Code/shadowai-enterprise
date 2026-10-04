package com.aigovernance.repository;

import com.aigovernance.model.ExceptionRequest;
import com.aigovernance.model.ExceptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ExceptionRequestRepository extends JpaRepository<ExceptionRequest,Long>{
 List<ExceptionRequest> findByTenantId(Long tenantId);
 List<ExceptionRequest> findByTenantIdAndStatus(Long tenantId,ExceptionStatus status);
 List<ExceptionRequest> findByStatusAndExpiresAtBefore(ExceptionStatus status,Instant now);
 List<ExceptionRequest> findByTenantIdAndStatusAndExpiresAtBefore(Long tenantId,ExceptionStatus status,Instant now);
 java.util.Optional<ExceptionRequest> findByTenantIdAndId(Long tenantId,Long id);
 boolean existsByTenantIdAndRequestIdAndStatus(Long tenantId,Long requestId,ExceptionStatus status);
}
