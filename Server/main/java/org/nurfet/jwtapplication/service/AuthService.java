package org.nurfet.jwtapplication.service;

import lombok.NonNull;
import org.nurfet.jwtapplication.dto.LoginRequest;
import org.nurfet.jwtapplication.dto.SignupRequest;
import org.nurfet.jwtapplication.dto.TokenResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AuthService {

    TokenResponse login(LoginRequest loginRequest);

    TokenResponse getAccessToken(@NonNull String refreshToken);

    TokenResponse refresh(String accessToken, String refreshToken);

    ResponseEntity<String> register(SignupRequest signUpRequest);

    void invalidateRefreshToken(UUID id);
}
