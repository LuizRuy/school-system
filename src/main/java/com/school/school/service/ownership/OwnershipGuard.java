package com.school.school.service.ownership;

import com.school.school.infra.security.UserAuthenticated;

public interface OwnershipGuard<T> {

    T resolve(Long id, UserAuthenticated principal);

    void authorize(Long id, UserAuthenticated principal);
}
