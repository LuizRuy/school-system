package com.school.school.infra.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtil {

    public static Long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || authentication.getDetails() == null){
            throw new RuntimeException("Authentication object is null");
        }

        return (Long)authentication.getDetails();
    }
}
