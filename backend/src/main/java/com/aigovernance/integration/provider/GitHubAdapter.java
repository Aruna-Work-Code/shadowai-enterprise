package com.aigovernance.integration.provider;
import org.springframework.stereotype.Component;
@Component public class GitHubAdapter extends HttpProviderAdapter {
 public String provider(){return "GITHUB";}
 public AdapterResult validate(String baseUrl,String credential){return get((baseUrl==null||baseUrl.isBlank()?"https://api.github.com/user":baseUrl),credential);}
}
