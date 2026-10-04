package com.aigovernance.integration.provider;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

abstract class HttpProviderAdapter implements ExternalProviderAdapter {
    protected AdapterResult get(String url,String credential){
        try {
            var b=HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).GET();
            if(credential!=null&&!credential.isBlank()) b.header("Authorization", authorization(credential));
            var r=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build().send(b.build(),HttpResponse.BodyHandlers.discarding());
            return new AdapterResult(r.statusCode()>=200&&r.statusCode()<300,r.statusCode(),"HTTP "+r.statusCode());
        } catch(Exception e){return new AdapterResult(false,0,e.getMessage()==null?"Provider connection failed":e.getMessage());}
    }
    protected String authorization(String credential){return credential.startsWith("Bearer ")?credential:"Bearer "+credential;}
}
