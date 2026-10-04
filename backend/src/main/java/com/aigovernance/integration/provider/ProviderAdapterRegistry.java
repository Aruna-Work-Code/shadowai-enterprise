package com.aigovernance.integration.provider;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class ProviderAdapterRegistry {
 private final Map<String,ExternalProviderAdapter> adapters;
 public ProviderAdapterRegistry(List<ExternalProviderAdapter> list){var m=new HashMap<String,ExternalProviderAdapter>();list.forEach(a->m.put(a.provider(),a));adapters=Map.copyOf(m);}
 public ExternalProviderAdapter get(String provider){var a=adapters.get(provider==null?"":provider.toUpperCase());if(a==null)throw new IllegalArgumentException("Unsupported provider: "+provider);return a;}
 public Set<String> providers(){return adapters.keySet();}
}
