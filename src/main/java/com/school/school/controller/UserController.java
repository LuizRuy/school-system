package com.school.school.controller;


import com.school.school.model.dto.UserRequest;
import com.school.school.model.dto.UserResponse;
import com.school.school.model.dto.UserUpdate;
import com.school.school.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserResponse> updateUser(@RequestBody UserUpdate userUpdate) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.update(userUpdate));
    }
}
