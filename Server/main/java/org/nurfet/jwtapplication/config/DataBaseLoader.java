package org.nurfet.jwtapplication.config;

import lombok.RequiredArgsConstructor;
import org.nurfet.jwtapplication.model.Role;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.repository.RoleRepository;
import org.nurfet.jwtapplication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class DataBaseLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        Role roleAdmin = addRole("ROLE_ADMIN");
        Role roleUser = addRole("ROLE_USER");

        addDefaultUser("Админ", "Админов","admin", "admin@mail.ru", roleAdmin);
        addDefaultUser("Евгения", "Лебедь","user", "user@mail.ru", roleUser);
        addDefaultUser("Александр", "Пушкин","alex", "alex@mail.ru", roleUser);
        addDefaultUser("Иван", "Иванов","ivan", "ivan@mail.ru", roleUser);
        addDefaultUser("Петр", "Петров","peter", "peter@mail.ru", roleUser);
        addDefaultUser("Сидор", "Сидоров","sidor", "sidor@mail.ru", roleUser);
        addDefaultUser("Светлана", "Светова","sveta", "sveta@mail.ru", roleUser);
        addDefaultUser("Константин", "Нарышкин","kostya", "kostya@mail.ru", roleUser);
    }

    private void addDefaultUser(String firstName, String lastName, String username,
                                String email, Role... roles) {

        userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User(
                    firstName,
                    lastName,
                    username,
                    email,
                    passwordEncoder.encode(username),
                    new HashSet<>(Arrays.asList(roles))
            );

            return userRepository.save(user);
        });
    }

    private Role addRole(String roleName) {
        return roleRepository.findRoleByAuthority(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
