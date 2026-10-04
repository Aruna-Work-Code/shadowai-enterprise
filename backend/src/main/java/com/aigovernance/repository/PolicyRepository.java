package com.aigovernance.repository;
import com.aigovernance.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PolicyRepository extends JpaRepository<Policy,Long>{
 List<Policy> findByTenantId(Long tenantId);
 java.util.Optional<Policy> findByTenantIdAndId(Long tenantId,Long id);
}
