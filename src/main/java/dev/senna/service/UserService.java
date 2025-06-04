package dev.senna.service;

import dev.senna.controller.CreateUserRequest;
import dev.senna.exception.UserAlreadyExists;
import dev.senna.model.entity.UserEntity;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    public UUID createUser(CreateUserRequest userReq) {
        if (userRepository.existsByUsername(userReq.username())) {
            throw new UserAlreadyExists("Username already in use");
        }

        var user = new UserEntity();
        user.setUsername(userReq.username());
        user.setPassword(BcryptUtil.bcryptHash(userReq.password()));
        user.setRole("USER");

        userRepository.persist(user);

        return user.getUserId();
    }
}
