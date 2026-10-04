package com.aigovernance.service;

import com.aigovernance.model.AuditEvent;
import com.aigovernance.repository.AuditRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

/**
 * Tamper-evident audit writer.
 *
 * The hash chain is useful for detecting modifications. For horizontally
 * scaled production deployments, the write path should additionally use
 * database serialization/locking or a dedicated append-only audit service.
 */
@Service
public class AuditService {
    private final AuditRepository repo;
    public AuditService(AuditRepository repo){this.repo=repo;}

    public synchronized void record(String type, Long id, String action,
                                     Long actor, String metadata){
        AuditEvent e=new AuditEvent();
        e.tenantId=TenantContext.currentOrDefault();
        e.entityType=type; e.entityId=id; e.action=action;
        e.actorId=actor; e.metadata=metadata; e.createdAt=Instant.now();

        String previous=repo.findTopByTenantIdOrderByIdDesc(e.tenantId)
                .map(x -> x.eventHash).orElse("");
        e.previousHash=previous;
        e.eventHash=sha256(e.tenantId+"|"+e.entityType+"|"+e.entityId+"|"+
                e.action+"|"+e.actorId+"|"+e.metadata+"|"+e.createdAt+"|"+previous);
        repo.save(e);
    }

    private String sha256(String value){
        try {
            byte[] b=MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder();
            for(byte x:b) out.append(String.format("%02x",x));
            return out.toString();
        } catch(Exception e){ throw new IllegalStateException("Audit hashing unavailable",e); }
    }
}
