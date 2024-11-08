package org.nurfet.jwtapplication.repository;

import org.nurfet.jwtapplication.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findRefreshTokenByUsername(String username);

    Optional<RefreshToken> findById(UUID id);

    void deleteById(UUID id);

    List<RefreshToken> findAllByUsername(String username);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r IN :tokens")
    void deleteAllTokens(@Param("tokens") List<RefreshToken> tokens);
}
