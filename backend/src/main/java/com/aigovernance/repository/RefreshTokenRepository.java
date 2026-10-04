package com.aigovernance.repository;
import com.aigovernance.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long>{
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
