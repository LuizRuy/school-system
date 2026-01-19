package com.school.school.controller;


import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.user.ChangePasswordRequest;
import com.school.school.model.dto.user.UserRequest;
import com.school.school.model.dto.user.UserResponse;
import com.school.school.model.dto.user.UserUpdate;
import com.school.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> createUser(@RequestBody UserRequest userRequest) {
        userService.save(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/update")
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserUpdate userUpdate,
                                                   @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(userUpdate, authenticatedUser));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
                                               @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        userService.changePassword(changePasswordRequest, authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
