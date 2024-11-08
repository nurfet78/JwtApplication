package org.nurfet.jwtapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.jwtapplication.dto.UserDTO;
import org.nurfet.jwtapplication.dto.UserUpdateRequest;
import org.nurfet.jwtapplication.dto.ValidationError;
import org.nurfet.jwtapplication.exception.UserNotFoundException;
import org.nurfet.jwtapplication.jwt.service.RefreshTokenService;
import org.nurfet.jwtapplication.model.RefreshToken;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    private final RefreshTokenService refreshTokenService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        return ResponseEntity.ok(userService.convertUsersToDTOs(userService.findAllUsers()));
    }

    @GetMapping("/editUser/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findUserById(id);

        return ResponseEntity.ok(userService.userToUserDto(user));
    }

    @PutMapping("/editUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateRequest user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return userService.getMapResponseEntity(bindingResult);
        }

        try {

            Optional<ValidationError> validationError = userService.validateUserData(user);

            if (validationError.isPresent()) {
                return ResponseEntity.badRequest().body(validationError.get().getMessage());
            }

            userService.updateUser(user);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Пользователь удален");
    }

    @DeleteMapping("/revokeRefreshToken/{userId}")
    public ResponseEntity<?> revokeRefreshToken(@PathVariable Long userId) {
        try {
            refreshTokenService.revokeAllUserTokens(userId);
            return ResponseEntity.ok()
                    .body("Все сессии пользователя успешно завершены");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при отзыве токена: " + e.getMessage());
        }
    }
}
