package org.nurfet.jwtapplication.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserUpdateRequest {

    private Long id;

    @Size(min = 2, max = 30, message = "Имя должно содержать от 2 до 30 символов")
    @NotBlank(message = "Имя должно быть указано")
    private String firstName;

    @Size(min = 2, max = 30, message = "Фамилия должна содержать от 2 до 30 символов")
    @NotBlank(message = "Фамилия должна быть указана")
    private String lastName;

    @Size(min = 3, max = 20, message = "Имя пользователя должно содержать от 3 до 30 символов")
    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Поле email должно быть заполнено")
    @Pattern(regexp = "^(\\w+\\.)*\\w+@(\\w+\\.)+[A-Za-z]+$", message = "Адрес электронной почты указан неверно")
    @Column(unique = true)
    private String email;

    @Size(min = 4, max = 8, message = "Пароль должен содержать от 4 до 8 символов")
    private String password;

    private List<String> roles;
}
