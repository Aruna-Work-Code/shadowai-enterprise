package com.aigovernance.controller;
import com.aigovernance.dto.WebhookEventRequest;
import com.aigovernance.security.HmacWebhookVerifier;
import com.aigovernance.service.IntegrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/webhooks")
public class WebhookController {
 private final IntegrationService service; private final HmacWebhookVerifier hmac; private final ObjectMapper mapper;
 public WebhookController(IntegrationService s,HmacWebhookVerifier h,ObjectMapper m){service=s;hmac=h;mapper=m;}
 @PostMapping("/events")
 public Object receive(@RequestHeader("X-Integration-Secret") String secret,
                       @RequestHeader(value="X-Integration-Signature",required=false) String signature,
                       @RequestHeader(value="X-Integration-Timestamp",required=false) String timestamp,
                       @Valid @RequestBody WebhookEventRequest request) throws Exception {
   String body=mapper.writeValueAsString(request); hmac.verify(secret,signature,timestamp,body); return service.receive(request,secret);
 }
}
