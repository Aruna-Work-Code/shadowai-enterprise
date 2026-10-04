package com.aigovernance.repository;
import com.aigovernance.model.IntegrationConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface IntegrationConnectionRepository extends JpaRepository<IntegrationConnection,Long>{
 List<IntegrationConnection> findByTenantId(Long tenantId);
 java.util.Optional<IntegrationConnection> findByTenantIdAndId(Long tenantId,Long id);
 List<IntegrationConnection> findByTenantIdAndProvider(Long tenantId,String provider);
}
