package com.school.school.service;

import com.school.school.infra.exception.BusinessException;
import com.school.school.infra.security.IssuedTokens;
import com.school.school.infra.security.TokenLifecycle;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.AuthMapper;
import com.school.school.model.User;
import com.school.school.model.dto.auth.AuthRequest;
import com.school.school.model.dto.auth.AuthResponse;
import com.school.school.model.dto.auth.RefreshTokenResponse;
import com.school.school.model.enums.Status;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenLifecycle tokenLifecycle;
    private final AuthMapper authMapper;

    public AuthResponse login(AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        UserAuthenticated principal = (UserAuthenticated) authentication.getPrincipal();
        User user = principal.getUser();

        if(user.getStatus() == Status.DISABLED) {
            throw new BusinessException("User have been disabled");
        }

        IssuedTokens tokens = tokenLifecycle.issuePair(user);

        return authMapper.toAuthResponse(tokens.accessToken(), tokens.refreshToken(), user);
    }

    public RefreshTokenResponse refreshToken(String token){
        IssuedTokens tokens = tokenLifecycle.rotate(token);
        return authMapper.toRefreshTokenResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
