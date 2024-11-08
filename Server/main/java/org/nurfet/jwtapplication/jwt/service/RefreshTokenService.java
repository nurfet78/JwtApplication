package org.nurfet.jwtapplication.jwt.service;

import org.nurfet.jwtapplication.model.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {

    RefreshToken findRefreshTokenByUsername(String username);

    RefreshToken saveRefreshToken(String username, String refreshToken);

    void invalidateRefreshToken(UUID id);

    void revokeAllUserTokens(Long userId);
}
