package com.school.school.controller;

import com.school.school.model.dto.auth.AuthRequest;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.dto.auth.RefreshTokenRequest;
import com.school.school.model.dto.auth.RefreshTokenResponse;
import com.school.school.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);
    }

    @PostMapping("/refresh")
    ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest){
        RefreshTokenResponse refreshTokenResponse = authService.refreshToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(refreshTokenResponse);
    }

}
