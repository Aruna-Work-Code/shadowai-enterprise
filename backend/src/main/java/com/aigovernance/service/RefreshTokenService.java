package com.aigovernance.service;

import com.aigovernance.model.RefreshToken;
import com.aigovernance.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository repo;
    private final long days;
    private final SecureRandom random = new SecureRandom();
    public RefreshTokenService(RefreshTokenRepository repo,@Value("${app.jwt.refresh-expiration-days:30}") long days){this.repo=repo;this.days=days;}
    public Issued issue(Long userId,Long tenantId,Long rotatedFrom){
        String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(random.generateSeed(48));
        RefreshToken t=new RefreshToken(); t.userId=userId;t.tenantId=tenantId;t.tokenHash=sha256(raw);t.expiresAt=Instant.now().plus(days, ChronoUnit.DAYS);t.rotatedFromId=rotatedFrom;repo.save(t);return new Issued(raw,t);
    }
    public RefreshToken validate(String raw){
        if(raw==null||raw.isBlank()) throw new IllegalArgumentException("Refresh token is required");
        RefreshToken t=repo.findByTokenHashAndRevokedFalse(sha256(raw)).orElseThrow(()->new IllegalArgumentException("Invalid refresh token"));
        if(t.expiresAt.isBefore(Instant.now())) { t.revoked=true;repo.save(t);throw new IllegalArgumentException("Refresh token expired"); }
        return t;
    }
    public void revoke(RefreshToken t){t.revoked=true;repo.save(t);}
    private String sha256(String v){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(v.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    public record Issued(String raw,RefreshToken entity){}
}
