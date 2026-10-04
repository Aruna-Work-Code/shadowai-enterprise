package com.aigovernance.repository;

import com.aigovernance.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByTenantIdAndId(Long tenantId, Long id);

    List<Notification> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<Notification> findByTenantIdAndUserIdOrderByCreatedAtDesc(Long tenantId, Long userId);
}
