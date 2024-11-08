package org.nurfet.jwtapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.jwtapplication.dto.*;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        try {
            // Получаем текущего пользователя из контекста безопасности
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = userService.findUserByUserName(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("roles", roles);
            response.put("lastLogin", LocalDateTime.now().toString());

            ApiResponse<Map<String, Object>> apiResponse =
                    new ApiResponse<>("Панель управления успешно загружена", response);

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Не удалось загрузить данные панели управления"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateRequest updateRequest,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return userService.getMapResponseEntity(bindingResult);
        }

        try {
            // Получаем текущего пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findUserByUserName(auth.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Проверяем данные на валидность
            Optional<ValidationError> validationError = userService.validateUserData(updateRequest);
            if (validationError.isPresent()) {
                return ResponseEntity.badRequest().body(validationError.get().getMessage());
            }

            // Обновляем пользователя
            userService.updateUser(updateRequest);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("Профиль успешно обновлен", null));

        } catch (Exception e) {
            log.error("Error updating user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
}
