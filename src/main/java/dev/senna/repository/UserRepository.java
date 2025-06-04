package dev.senna.repository;

import dev.senna.controller.CreateUserRequest;
import dev.senna.model.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, UUID> {

    public boolean existsByUsername(String username) {
        return count("username", username) > 0;
    }
}
