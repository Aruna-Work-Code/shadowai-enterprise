package com.aigovernance.integration.provider;
import org.springframework.stereotype.Component;
@Component public class SiemWebhookAdapter implements ExternalProviderAdapter {
 public String provider(){return "SIEM_WEBHOOK";}
 public AdapterResult validate(String baseUrl,String credential){return new AdapterResult(baseUrl!=null&&!baseUrl.isBlank()&&credential!=null&&!credential.isBlank(),0,"Webhook endpoint and credential configured");}
}
