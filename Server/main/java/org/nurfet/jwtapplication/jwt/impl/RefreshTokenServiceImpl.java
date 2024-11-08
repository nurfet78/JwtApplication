package org.nurfet.jwtapplication.jwt.impl;

import lombok.RequiredArgsConstructor;
import org.nurfet.jwtapplication.exception.UserNotFoundException;
import org.nurfet.jwtapplication.jwt.JwtConfig.JwtConfig;
import org.nurfet.jwtapplication.jwt.service.RefreshTokenService;
import org.nurfet.jwtapplication.model.RefreshToken;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.repository.RefreshTokenRepository;
import org.nurfet.jwtapplication.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    private final JwtConfig jwtConfig;

    @Override
    public RefreshToken findRefreshTokenByUsername(String username) {
        return refreshTokenRepository.findRefreshTokenByUsername(username)
                .orElse(null);
    }

    @Override
    public RefreshToken saveRefreshToken(String username, String refreshToken) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            RefreshToken existingToken = user.getRefreshToken();

            final LocalDateTime now = LocalDateTime.now();
            final Instant issuedAtInstant = now.atZone(ZoneId.systemDefault()).toInstant();
            final Instant expirationTime = now.plusMinutes(jwtConfig.getRefreshTokenExpiration()).atZone(ZoneId.systemDefault()).toInstant();

            if (existingToken != null) {
                // Обновляем существующий refresh-токен
                existingToken.setToken(refreshToken);
                existingToken.setExpirationTime(expirationTime);
                existingToken.setCreationTime(issuedAtInstant);
                existingToken.setUsername(username);
                user.setRefreshToken(existingToken);
                return refreshTokenRepository.save(existingToken);
            } else {
                // Создаем и сохраняем новый refresh-токен
                RefreshToken newToken = new RefreshToken(username, refreshToken, expirationTime, issuedAtInstant, user);
                user.setRefreshToken(newToken);
                return refreshTokenRepository.save(newToken);
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Transactional
    @Override
    public void invalidateRefreshToken(UUID id) {

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(id);

        if (refreshToken.isEmpty()) {
            throw new NoSuchElementException("Refresh токен не найден или уже аннулирован");
        }

        refreshTokenRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        List<RefreshToken> userTokens = refreshTokenRepository.findAllByUsername(user.getUsername());
        if (!userTokens.isEmpty()) {
            deleteAllTokens(userTokens);
        }
    }

    private void deleteAllTokens(List<RefreshToken> userTokens) {
        refreshTokenRepository.deleteAllTokens(userTokens);
    }
}
