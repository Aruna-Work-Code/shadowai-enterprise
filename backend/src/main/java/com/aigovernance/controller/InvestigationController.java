package com.aigovernance.controller;
import com.aigovernance.model.Investigation;
import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController @RequestMapping("/api/v1/investigations")
public class InvestigationController {
 private final InvestigationRepository repo;private final AuditService audit;
 public InvestigationController(InvestigationRepository r,AuditService a){repo=r;audit=a;}
 @GetMapping public List<Investigation> all(){return repo.findByTenantId(TenantContext.currentOrDefault());}
 @PostMapping("/{id}/assign") @PreAuthorize("hasAnyRole('SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
 public Investigation assign(@PathVariable Long id,@RequestParam Long ownerId){
  var i=repo.findByTenantIdAndId(TenantContext.currentOrDefault(),id).orElseThrow();i.ownerId=ownerId;i.status="IN_PROGRESS";i=repo.save(i);audit.record("INVESTIGATION",id,"ASSIGNED",ownerId,"owner="+ownerId);return i;
 }
 @PostMapping("/{id}/resolve") @PreAuthorize("hasAnyRole('SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
 public Investigation resolve(@PathVariable Long id,@RequestParam String resolution){
  var i=repo.findByTenantIdAndId(TenantContext.currentOrDefault(),id).orElseThrow();i.status="RESOLVED";i.resolution=resolution;i.resolvedAt=Instant.now();i=repo.save(i);audit.record("INVESTIGATION",id,"RESOLVED",null,resolution);return i;
 }
}
