package com.school.school.controller;


import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.user.*;
import com.school.school.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> createUser(@RequestBody @Valid UserRequest userRequest) {
        userService.save(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@RequestBody @Valid UserUpdate userUpdate,
                                                   @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(userUpdate, authenticatedUser));
    }

    @PatchMapping("/disable/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.disable(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest,
                                               @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        userService.changePassword(changePasswordRequest, authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
