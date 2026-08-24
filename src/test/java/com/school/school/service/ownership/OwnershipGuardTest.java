package com.school.school.service.ownership;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Task;
import com.school.school.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OwnershipGuardTest {

    private final Map<Long, Task> tasks = new HashMap<>();

    private OwnershipGuard<Task> guard() {
        return guard(OwnershipErrorMode.FORBIDDEN);
    }

    private OwnershipGuard<Task> guard(OwnershipErrorMode errorMode) {
        return OwnershipGuards.ownedBy(
                id -> Optional.ofNullable(tasks.get(id)),
                task -> task.getUser().getId(),
                "Task",
                errorMode
        );
    }

    private User userWithId(long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private UserAuthenticated principal(User user) {
        return new UserAuthenticated(user);
    }

    private Task ownedTask(long taskId, long ownerId) {
        Task task = new Task();
        task.setId(taskId);
        task.setUser(userWithId(ownerId));
        return task;
    }

    @Test
    @DisplayName("Resolves an entity owned by the principal")
    void resolvesEntityOwnedByPrincipal() {
        Task owned = ownedTask(42L, 7L);
        tasks.put(42L, owned);

        Task resolved = guard().resolve(42L, principal(userWithId(7L)));

        assertThat(resolved).isSameAs(owned);
    }

    @Test
    @DisplayName("A missing entity is reported as not found")
    void missingEntityIsReportedAsNotFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard().resolve(99L, principal(userWithId(7L))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found with id: 99");
    }

    @Test
    @DisplayName("A foreign entity is denied when the error mode is FORBIDDEN")
    void foreignEntityIsDeniedInForbiddenMode() {
        tasks.put(42L, ownedTask(42L, 8L));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard().resolve(42L, principal(userWithId(7L))))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this task");
    }

    @Test
    @DisplayName("A foreign entity is masked as not found when the error mode is NOT_FOUND")
    void foreignEntityIsMaskedAsNotFoundInNotFoundErrorMode() {
        tasks.put(42L, ownedTask(42L, 8L));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard(OwnershipErrorMode.NOT_FOUND).resolve(42L, principal(userWithId(7L))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found with id: 42");
    }

    @Test
    @DisplayName("Authorize passes silently for an entity owned by the principal and returns nothing to discard")
    void authorizePassesSilentlyForOwnedEntity() {
        tasks.put(42L, ownedTask(42L, 7L));

        guard().authorize(42L, principal(userWithId(7L)));
    }

    @Test
    @DisplayName("Authorize reports a missing entity as not found")
    void authorizeReportsMissingEntityAsNotFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard().authorize(99L, principal(userWithId(7L))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found with id: 99");
    }

    @Test
    @DisplayName("Authorize denies a foreign entity when the error mode is FORBIDDEN")
    void authorizeDeniesForeignEntityInForbiddenMode() {
        tasks.put(42L, ownedTask(42L, 8L));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard().authorize(42L, principal(userWithId(7L))))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this task");
    }

    @Test
    @DisplayName("Authorize masks a foreign entity as not found when the error mode is NOT_FOUND")
    void authorizeMasksForeignEntityInNotFoundErrorMode() {
        tasks.put(42L, ownedTask(42L, 8L));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> guard(OwnershipErrorMode.NOT_FOUND).authorize(42L, principal(userWithId(7L))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Task not found with id: 42");
    }

}
