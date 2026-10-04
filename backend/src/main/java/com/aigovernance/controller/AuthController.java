package com.aigovernance.controller;
import com.aigovernance.dto.LoginRequest;
import com.aigovernance.repository.UserRepository;
import com.aigovernance.repository.TenantMembershipRepository;
import com.aigovernance.security.JwtService;
import com.aigovernance.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users;private final PasswordEncoder encoder;private final JwtService jwt; private final TenantMembershipRepository memberships; private final RefreshTokenService refreshTokens;
 public AuthController(UserRepository u,PasswordEncoder e,JwtService j,TenantMembershipRepository m,RefreshTokenService r){users=u;encoder=e;jwt=j;memberships=m;refreshTokens=r;}
 @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody LoginRequest r){
  var u=users.findByUsername(r.username()).orElseThrow(()->new RuntimeException("Invalid credentials"));
  if(!encoder.matches(r.password(),u.passwordHash))throw new RuntimeException("Invalid credentials");
  var membership = memberships.findFirstByUserId(u.id).orElse(null);
  Long tenantId = membership == null ? 1L : membership.tenantId;
  var issued=refreshTokens.issue(u.id,tenantId,null); return Map.of("token",jwt.generate(u.username,u.role.name(),tenantId),"refreshToken",issued.raw(),"username",u.username,"role",u.role.name(),"tenantId",tenantId);
 }
}
