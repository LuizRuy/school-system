package com.school.school.service.ownership;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.function.Function;

record RepositoryOwnershipGuard<T>(
        Function<Long, Optional<T>> finder,
        Function<T, Long> ownerIdOf,
        Function<Long, Optional<Long>> ownerIdInStorage,
        String entityType,
        OwnershipErrorMode errorMode
) implements OwnershipGuard<T> {

    @Override
    public T resolve(Long id, UserAuthenticated principal) {
        T entity = finder.apply(id)
                .orElseThrow(() -> notFound(id));

        denyForeign(id, principal, ownerIdOf.apply(entity));

        return entity;
    }

    @Override
    public void authorize(Long id, UserAuthenticated principal) {
        if (ownerIdInStorage == null) {
            resolve(id, principal);
            return;
        }

        Long storedOwnerId = ownerIdInStorage.apply(id)
                .orElseThrow(() -> notFound(id));

        denyForeign(id, principal, storedOwnerId);
    }

    private void denyForeign(Long id, UserAuthenticated principal, Long ownerId) {
        if (ownerId.equals(principal.getUser().getId())) {
            return;
        }
        if (errorMode == OwnershipErrorMode.NOT_FOUND) {
            throw notFound(id);
        }
        throw new AccessDeniedException("You do not have permission to access this " + entityType.toLowerCase());
    }

    private EntityNotFoundException notFound(Long id) {
        return new EntityNotFoundException(entityType + " not found with id: " + id);
    }
}
