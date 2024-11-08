package org.nurfet.jwtapplication.service;

import org.nurfet.jwtapplication.dto.SignupRequest;
import org.nurfet.jwtapplication.dto.UserUpdateRequest;
import org.nurfet.jwtapplication.dto.ValidationError;
import org.nurfet.jwtapplication.dto.UserDTO;
import org.nurfet.jwtapplication.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {

    User createUser(SignupRequest signupRequest);

    Optional<User> findUserByUserName(String username);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    List<User> findAllUsers();

    User findUserById(Long id);

    User updateUser(UserUpdateRequest userUpdateRequest);

    void deleteUserById(Long id);

    ResponseEntity<Map<String, Object>> getMapResponseEntity(BindingResult bindingResult);

    Optional<ValidationError> validateUserData(UserUpdateRequest user);

    List<UserDTO> convertUsersToDTOs(List<User> users);

    UserDTO userToUserDto(User user);
}
