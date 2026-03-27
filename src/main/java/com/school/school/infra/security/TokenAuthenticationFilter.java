package com.school.school.infra.security;

import com.school.school.model.User;
import com.school.school.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.validateToken(token);

                Long userId = claims.get("userId", Long.class);
                String role = claims.get("role", String.class);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new JwtException("Usuário não encontrado"));

                UserAuthenticated userAuth = new UserAuthenticated(user);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userAuth,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (ExpiredJwtException e) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response,
                        new CredentialsExpiredException("Token expirado"));
                return;

            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response,
                        new BadCredentialsException("Token inválido"));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
