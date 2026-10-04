package com.aigovernance.repository;
import com.aigovernance.model.IntegrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent,Long>{
 Optional<IntegrationEvent> findByTenantIdAndProviderAndProviderEventId(Long tenantId,String provider,String providerEventId);
 List<IntegrationEvent> findByTenantIdAndProcessingStatus(Long tenantId,String processingStatus);
 long countByTenantId(Long tenantId);
}
