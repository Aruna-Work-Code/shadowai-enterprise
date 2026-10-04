package com.aigovernance.controller;

import com.aigovernance.repository.NotificationRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository repository;

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns tenant-wide governance notifications.
     * System-generated discovery events use a null userId because they
     * are governance signals rather than private employee messages.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Object list() {
        return repository.findByTenantIdOrderByCreatedAtDesc(
                TenantContext.currentOrDefault());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public Object listForUser(@PathVariable Long userId) {
        return repository.findByTenantIdAndUserIdOrderByCreatedAtDesc(
                TenantContext.currentOrDefault(), userId);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Object read(@PathVariable Long id) {
        var notification = repository
                .findByTenantIdAndId(TenantContext.currentOrDefault(), id)
                .orElseThrow();
        notification.readFlag = true;
        return repository.save(notification);
    }
}
