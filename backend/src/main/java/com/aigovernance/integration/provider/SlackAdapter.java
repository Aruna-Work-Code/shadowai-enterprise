package com.aigovernance.integration.provider;
import org.springframework.stereotype.Component;
@Component public class SlackAdapter extends HttpProviderAdapter {
 public String provider(){return "SLACK";}
 public AdapterResult validate(String baseUrl,String credential){return get((baseUrl==null||baseUrl.isBlank()?"https://slack.com/api/auth.test":baseUrl),credential);}
}
