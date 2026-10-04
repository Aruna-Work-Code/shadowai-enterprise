package com.aigovernance.integration.provider;
import org.springframework.stereotype.Component;
@Component public class OktaAdapter extends HttpProviderAdapter {
 public String provider(){return "OKTA";}
 public AdapterResult validate(String baseUrl,String credential){return get((baseUrl==null||baseUrl.isBlank()?"/api/v1/users?limit=1":baseUrl),credential);}
}
