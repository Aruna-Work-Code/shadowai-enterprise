package com.aigovernance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class HmacWebhookVerifier {
    private final boolean required; private final long maxSkewSeconds;
    public HmacWebhookVerifier(@Value("${app.webhook.hmac-required:false}") boolean required,@Value("${app.webhook.max-skew-seconds:300}") long maxSkewSeconds){this.required=required;this.maxSkewSeconds=maxSkewSeconds;}
    public void verify(String secret,String signature,String timestamp,String body){
        if(!required && (signature==null||signature.isBlank())) return;
        if(signature==null||timestamp==null) throw new IllegalArgumentException("Webhook signature and timestamp are required");
        long ts; try{ts=Long.parseLong(timestamp);}catch(NumberFormatException e){throw new IllegalArgumentException("Invalid webhook timestamp");}
        if(Math.abs(Instant.now().getEpochSecond()-ts)>maxSkewSeconds) throw new IllegalArgumentException("Webhook timestamp outside allowed window");
        String expected=sign(secret,timestamp+"."+body);
        String supplied=signature.startsWith("sha256=")?signature.substring(7):signature;
        if(!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException("Invalid webhook signature");
    }
    public String sign(String secret,String message){try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));return HexFormat.of().formatHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException("HMAC unavailable",e);}}
}
