package org.nurfet.jwtapplication.dto;

import lombok.Getter;

@Getter
public enum ValidationError {

    USERNAME_TAKEN("Имя пользователя уже занято!"),
    EMAIL_TAKEN("Электронная почта уже используется!");

    private final String message;

    ValidationError(String message) {
        this.message = message;
    }
}
