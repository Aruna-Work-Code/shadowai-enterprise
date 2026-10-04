package com.aigovernance.service;

import com.aigovernance.dto.PolicyUpsertRequest;
import com.aigovernance.model.Policy;
import com.aigovernance.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import com.aigovernance.tenant.TenantContext;
import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository repo;
    public PolicyService(PolicyRepository repo){this.repo=repo;}

    public List<Policy> all(){ return repo.findByTenantId(TenantContext.currentOrDefault()); }

    public Policy create(PolicyUpsertRequest r){
        return repo.save(apply(new Policy(), r));
    }

    public Policy update(Long id, PolicyUpsertRequest r){
        Policy p=repo.findByTenantIdAndId(TenantContext.currentOrDefault(), id).orElseThrow(
                () -> new IllegalArgumentException("Policy not found"));
        return repo.save(apply(p,r));
    }

    public void disable(Long id){
        Policy p=repo.findByTenantIdAndId(TenantContext.currentOrDefault(), id).orElseThrow(
                () -> new IllegalArgumentException("Policy not found"));
        p.enabled=false; repo.save(p);
    }

    private Policy apply(Policy p, PolicyUpsertRequest r){
        p.name=r.name().trim(); p.dataType=r.dataType().trim().toUpperCase();
        p.allowedTools=r.allowedTools(); p.prohibitedTools=r.prohibitedTools();
        p.approvalRequired=r.approvalRequired(); p.riskLevel=r.riskLevel();
        p.exceptionAllowed=r.exceptionAllowed(); p.maxDurationDays=r.maxDurationDays();
        p.enabled=r.enabled(); p.description=r.description();
        return p;
    }
}
