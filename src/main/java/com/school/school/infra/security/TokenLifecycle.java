package com.school.school.infra.security;

import com.school.school.model.User;
import io.jsonwebtoken.Claims;

public interface TokenLifecycle {

    IssuedTokens issuePair(User user);

    IssuedTokens rotate(String rawRefreshToken);

    Claims validateAccess(String rawAccessToken);
}
