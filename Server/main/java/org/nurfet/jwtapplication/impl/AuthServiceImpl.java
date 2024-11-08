package org.nurfet.jwtapplication.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.jwtapplication.dto.LoginRequest;
import org.nurfet.jwtapplication.dto.SignupRequest;
import org.nurfet.jwtapplication.dto.TokenResponse;
import org.nurfet.jwtapplication.exception.UserNotFoundException;
import org.nurfet.jwtapplication.jwt.service.RefreshTokenService;
import org.nurfet.jwtapplication.jwt.util.JwtUtil;
import org.nurfet.jwtapplication.model.RefreshToken;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.service.AuthService;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;

    private final RefreshTokenService refreshTokenService;

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            return getJwtResponse(user);

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Неверное имя пользователя или пароль.");
        }
    }

    @Override
    public TokenResponse getAccessToken(@NonNull String refreshToken) {
        if (jwtUtil.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtUtil.getRefreshClaims(refreshToken);
            final String username = claims.getSubject();
            final RefreshToken savedRefreshToken = refreshTokenService.findRefreshTokenByUsername(username);


            if (savedRefreshToken != null && savedRefreshToken.getToken().equals(refreshToken)) {
                final User user = userService.findUserByUserName(username)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

                final String accessToken = jwtUtil.generateToken(user, false);

                return createTokenResponse(user, accessToken, null, savedRefreshToken.getId());
            }
        }
        throw new BadCredentialsException("Внутренняя ошибка сервера. Пожалуйста, повторите попытку позже.");
    }

    @Override
    public TokenResponse refresh(String accessToken, String refreshToken) {

        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        if (jwtUtil.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtUtil.getRefreshClaims(refreshToken);
            final String username = claims.getSubject();

            // Проверяем, что access токен принадлежит тому же пользователю
            try {
                Claims accessClaims = jwtUtil.getAccessClaims(accessToken);
                if (!username.equals(accessClaims.getSubject())) {
                    throw new BadCredentialsException("Токены не совпадают");
                }
            } catch (ExpiredJwtException e) {
                log.info("Access токен истек, это нормально");
            }

            final RefreshToken saveRefreshToken = refreshTokenService.findRefreshTokenByUsername(username);

            if (saveRefreshToken != null && saveRefreshToken.getToken().equals(refreshToken)) {
                User user = userService.findUserByUserName(username)
                        .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

                return getJwtResponse(user);
            }
        }

        throw new BadCredentialsException("Недействительный refresh токен");
    }

    @Override
    public ResponseEntity<String> register(SignupRequest signUpRequest) {

        if (userService.existsByUserName(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body("Имя пользователя уже занято!");
        }

        if (userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Электронная почта уже используется!");
        }

        userService.createUser(signUpRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("Регистрация прошла успешно! Пожалуйста войдите в систему.");
    }

    @Override
    public void invalidateRefreshToken(UUID id) {
        refreshTokenService.invalidateRefreshToken(id);
    }

    private TokenResponse getJwtResponse(User user) {
        final String accessToken = jwtUtil.generateToken(user, false);
        final String refreshToken = jwtUtil.generateToken(user, true);

        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);
        RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);

        return createTokenResponse(user, accessToken, refreshToken, savedRefreshToken.getId());
    }

    private TokenResponse createTokenResponse(User user, String accessToken, String refreshToken, UUID refreshTokenId) {

        String roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return new TokenResponse(accessToken, refreshToken, "Bearer",
                refreshTokenId, user.getId(), user.getUsername(), user.getEmail(), roles);
    }
}
