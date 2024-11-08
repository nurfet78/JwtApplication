package org.nurfet.jwtapplication.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.jwtapplication.dto.LoginRequest;
import org.nurfet.jwtapplication.dto.SignupRequest;
import org.nurfet.jwtapplication.model.RefreshJwtRequest;
import org.nurfet.jwtapplication.service.AuthService;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    private final AuthService authService;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest,
                                          BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return userService.getMapResponseEntity(bindingResult);
        }

        return authService.register(signUpRequest);
    }

    @PostMapping("/token")
    public ResponseEntity<?> getNewAccessToken(@RequestBody RefreshJwtRequest request) {

        return ResponseEntity.ok(authService.getAccessToken(request.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(@RequestBody RefreshJwtRequest request) {

        return ResponseEntity.ok(authService.refresh(request.getAccessToken(), request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        try {
            String refreshTokenIdStr = body.get("refreshTokenId");
            if (refreshTokenIdStr == null) {
                return ResponseEntity.badRequest().body("RefreshTokenId не может быть пустым");
            }

            UUID refreshTokenId;
            try {
                refreshTokenId = UUID.fromString(refreshTokenIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Неверный формат UUID");
            }

            authService.invalidateRefreshToken(refreshTokenId);
            return ResponseEntity.ok().body("Выход из системы выполнен успешно");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при выходе из системы: " + e.getMessage());
        }
    }
}
