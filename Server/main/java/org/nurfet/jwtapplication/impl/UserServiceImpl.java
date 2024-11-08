package org.nurfet.jwtapplication.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.jwtapplication.dto.SignupRequest;
import org.nurfet.jwtapplication.dto.UserDTO;
import org.nurfet.jwtapplication.dto.UserUpdateRequest;
import org.nurfet.jwtapplication.dto.ValidationError;
import org.nurfet.jwtapplication.model.Role;
import org.nurfet.jwtapplication.model.User;
import org.nurfet.jwtapplication.repository.RoleRepository;
import org.nurfet.jwtapplication.repository.UserRepository;
import org.nurfet.jwtapplication.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;


    @Override
    @Transactional
    public User createUser(SignupRequest signupRequest) {
        User user = new User();
        return populateUserData(signupRequest, user);
    }

    @Override
    public User updateUser(UserUpdateRequest userUpdateRequest) {
        User existingUser = findUserById(userUpdateRequest.getId());
        return populateUserData(userUpdateRequest, existingUser);
    }

    @Override
    public Optional<User> findUserByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUserName(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMapResponseEntity(BindingResult bindingResult) {
        List<Map<String, String>> errors = bindingResult.getFieldErrors().stream()
                .map(err -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("field", err.getField());
                    error.put("defaultMessage", err.getDefaultMessage());
                    return error;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @Override
    public Optional<ValidationError> validateUserData(UserUpdateRequest user) {
        User existingUser = findUserById(user.getId());
        String newUsername = user.getUsername();

        if (!existingUser.getUsername().equals(newUsername)) {
            if (existsByUserName(newUsername)) {
                return Optional.of(ValidationError.USERNAME_TAKEN);
            }
        }

        if (!existingUser.getEmail().equals(user.getEmail())) {
            if (existsByEmail(user.getEmail())) {
                return Optional.of(ValidationError.EMAIL_TAKEN);
            }
        }

        return Optional.empty();
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    private User populateUserData(Object source, User user) {

        switch (source) {
            case SignupRequest signupRequest -> {
                populateBasicData(user,
                        signupRequest.getFirstName(),
                        signupRequest.getLastName(),
                        signupRequest.getUsername(),
                        signupRequest.getEmail());
                user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
                // Если роли не указаны (обычная регистрация), устанавливаем ROLE_USER
                if (signupRequest.getRoles() == null || signupRequest.getRoles().isEmpty()) {
                    Role userRole = getOrCreateRole("ROLE_USER");
                    user.getRoles().add(userRole);
                } else {
                    // Если роли указаны (регистрация администратором), устанавливаем их
                    signupRequest.getRoles().forEach(roleName -> {
                        Role role = getOrCreateRole(roleName);
                        user.getRoles().add(role);
                    });
                }
            }
            case UserUpdateRequest updateRequest -> {
                populateBasicData(user,
                        updateRequest.getFirstName(),
                        updateRequest.getLastName(),
                        updateRequest.getUsername(),
                        updateRequest.getEmail());
                if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
                }

                if (updateRequest.getRoles() != null) {
                    user.removeRoles();
                    updateRequest.getRoles().forEach(roleName -> {
                        Role role = getOrCreateRole(roleName);
                        user.getRoles().add(role);
                    });
                }
            }
            default -> throw new IllegalArgumentException("Unsupported source type: " + source.getClass());
        }

        return userRepository.save(user);
    }

    @Override
    public List<UserDTO> convertUsersToDTOs(List<User> users) {
        return users.stream().map(user -> {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setRoles(user.getRoles().stream()
                    .map(Role::getAuthority)
                    .collect(Collectors.toSet()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public UserDTO userToUserDto(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                passwordEncoder.encode(user.getPassword()),
                user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toSet()));
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findRoleByAuthority(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private void populateBasicData(User user, String firstName, String lastName,
                                   String username, String email) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
    }
}
