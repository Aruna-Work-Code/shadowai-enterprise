package com.aigovernance.controller;
import com.aigovernance.integration.provider.ProviderAdapterRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/integration-providers")
public class ProviderAdapterController {
 private final ProviderAdapterRegistry registry;
 public ProviderAdapterController(ProviderAdapterRegistry r){registry=r;}
 @GetMapping @PreAuthorize("hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')") public Map<String,Object> providers(){return Map.of("providers",registry.providers());}
 @PostMapping("/{provider}/validate") @PreAuthorize("hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')") public Object validate(@PathVariable String provider,@RequestParam String baseUrl,@RequestParam String credential){return registry.get(provider).validate(baseUrl,credential);}
}
