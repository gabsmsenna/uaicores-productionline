package dev.senna.repository;

import dev.senna.model.entity.UserEntity;
import dev.senna.model.enums.UserRole;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }

    public boolean existsByRole(UserRole role) {
        return count("role", role) > 0;
    }

    public Optional<UserEntity> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        return find("username = ?1", username.trim()).firstResultOptional();
    }

    public UserEntity findUserByUsername(String username) {
        return findByUsername(username).orElse(null);
    }

}
