package com.aigovernance.controller;

import com.aigovernance.dto.RefreshTokenRequest;
import com.aigovernance.repository.TenantMembershipRepository;
import com.aigovernance.repository.UserRepository;
import com.aigovernance.security.JwtService;
import com.aigovernance.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class TokenController {
    private final RefreshTokenService refresh; private final UserRepository users; private final TenantMembershipRepository memberships; private final JwtService jwt;
    public TokenController(RefreshTokenService r,UserRepository u,TenantMembershipRepository m,JwtService j){refresh=r;users=u;memberships=m;jwt=j;}
    @PostMapping("/refresh") public Map<String,Object> refresh(@Valid @RequestBody RefreshTokenRequest req){
        var old=refresh.validate(req.refreshToken()); var user=users.findById(old.userId).orElseThrow(()->new IllegalArgumentException("User not found"));
        if(memberships.findByTenantIdAndUserId(old.tenantId,user.id).isEmpty()) throw new IllegalArgumentException("User is not a tenant member");
        refresh.revoke(old); var issued=refresh.issue(user.id,old.tenantId,old.id); return Map.of("token",jwt.generate(user.username,user.role.name(),old.tenantId),"refreshToken",issued.raw(),"tenantId",old.tenantId,"username",user.username,"role",user.role.name());
    }
    @PostMapping("/logout") public Map<String,Object> logout(@Valid @RequestBody RefreshTokenRequest req){var t=refresh.validate(req.refreshToken());refresh.revoke(t);return Map.of("status","LOGGED_OUT");}
}
