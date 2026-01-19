package com.school.school.service;

import com.school.school.infra.security.JwtUtil;
import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthRequest;
import com.school.school.model.dto.auth.AuthResponse;
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

    public AuthResponse login(AuthRequest authRequest) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = findByEmail(userDetails.getUsername());

            String jwt = jwtUtil.generateToken(user);

            return new AuthResponse(
                    jwt,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole().name()
            );
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
