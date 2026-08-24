package com.school.school.service.ownership;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.function.Function;

record RepositoryOwnershipGuard<T>(
        Function<Long, Optional<T>> finder,
        Function<T, Long> ownerIdOf,
        String entityType,
        OwnershipErrorMode errorMode
) implements OwnershipGuard<T> {

    @Override
    public T resolve(Long id, UserAuthenticated principal) {
        T entity = finder.apply(id)
                .orElseThrow(() -> notFound(id));

        if (!ownerIdOf.apply(entity).equals(principal.getUser().getId())) {
            if (errorMode == OwnershipErrorMode.NOT_FOUND) {
                throw notFound(id);
            }
            throw new AccessDeniedException("You do not have permission to access this " + entityType.toLowerCase());
        }

        return entity;
    }

    private EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException(entityType + " not found with id: " + id);
    }
}
