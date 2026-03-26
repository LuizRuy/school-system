package com.school.school.service;

import com.school.school.infra.exception.BusinessException;
import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.JwtUtil;
import com.school.school.infra.security.RefreshTokenService;
import com.school.school.model.RefreshToken;
import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthRequest;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.dto.auth.RefreshTokenResponse;
import com.school.school.model.enums.Status;
import com.school.school.repository.RefreshTokenRepository;
import com.school.school.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse login(AuthRequest authRequest) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = findByEmail(userDetails.getUsername());

            if(user.getStatus() == Status.DISABLED) {
                throw new BusinessException("User have been disabled");
            }

            String jwt = jwtUtil.generateToken(user);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

            return new AuthResponse(
                    jwt,
                    refreshToken.getToken(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole().name()
            );
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    public RefreshTokenResponse refreshToken(String token){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token not found"));

        refreshTokenService.verifyExpiration(refreshToken);

        String newToken = jwtUtil.generateToken(refreshToken.getUser());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUser().getEmail());

        return new RefreshTokenResponse(newToken, newRefreshToken.getToken());
    }
}
