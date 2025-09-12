package dev.senna.service;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.controller.dto.response.ListUserResponse;
import dev.senna.controller.dto.response.GetUserByIdResponse;
import dev.senna.controller.dto.request.UpdateUserDto;
import dev.senna.controller.dto.response.UpdateUserRespDto;
import dev.senna.exception.UserAlreadyExistsException;
import dev.senna.exception.UserNotFoundException;
import dev.senna.model.entity.UserEntity;
import dev.senna.model.enums.UserRole;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    SecurityIdentity identity;

    @Inject
    private UserRepository userRepository;

    private UserRole getCurrentUserRole() {
        if (identity.getRoles().contains("DEV")) return UserRole.DEV;
        if (identity.getRoles().contains("ADMIN")) return UserRole.ADMIN;
        if (identity.getRoles().contains("OFFICER")) return UserRole.OFFICER;
        throw new ForbiddenException("Usuário sem role válida para esta operação.");
    }

    @Transactional
    public void createUserBySystem(CreateUserRequest userReq) {
        var user = new UserEntity();
        user.setUsername(userReq.username());
        user.setPassword(BcryptUtil.bcryptHash(userReq.password()));
        user.setRole(userReq.role());

        userRepository.persist(user);
    }

    public UUID createUser(CreateUserRequest userReq) {

        UserRole requesterRole = getCurrentUserRole();

        if (requesterRole == UserRole.DEV) {
        } else if (requesterRole == UserRole.ADMIN) {
            if (userReq.role() != UserRole.OFFICER) {
                throw new ForbiddenException("ADMIN só pode criar usuários do tipo OFFICER.");
            }
        } else {
            throw new ForbiddenException("Você não possui permissão para criar usuários.");
        }

        log.info("Attempting to create a new user with username: {}", userReq.username());

        verifyIfUsernameAlreadyInUse(userReq.username());

        var user = new UserEntity();
        user.setUsername(userReq.username());
        user.setPassword(BcryptUtil.bcryptHash(userReq.password()));
        user.setRole(userReq.role());

        userRepository.persist(user);

        log.info("User {} created successfully with ID: {}", user.getUsername(), user.getUserId());
        return user.getUserId();
    }

    public List<ListUserResponse> findAll(Integer page, Integer pageSize) {
        log.debug("Fetching all users with page: {} and pageSize: {}", page, pageSize);
        var users = userRepository.findAll()
                .page(page, pageSize)
                .list();

        log.debug("Found {} users.", users.size());
        return users.stream()
                .map(userEntity -> new ListUserResponse(
                        userEntity.getUsername(),
                        userEntity.getRole().getRoleName()
                )).toList();
    }

    public GetUserByIdResponse findUserById(UUID id) {
        log.debug("Attempting to find user by ID: {}", id);

        var user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}. Throwing UserNotFoundException.", id);
                    return new UserNotFoundException(id);
                });

        log.debug("User found successfully with ID: {}", id);
        return user.toResponse();
    }

    public void updateUser(UUID userId, UpdateUserDto updateUserRequest) {
        log.info("Attempting to update user with ID: {}", userId);

        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for update with ID: {}. Throwing UserNotFoundException.", userId);
                    return new UserNotFoundException(userId);
                });

        if (!user.getUsername().equals(updateUserRequest.username())) {
            verifyIfUsernameAlreadyInUse(updateUserRequest.username());
        }

        user.setUsername(updateUserRequest.username());
        user.setPassword(BcryptUtil.bcryptHash(updateUserRequest.password()));

        userRepository.persist(user);

        log.info("User with ID: {} updated successfully.", userId);
    }

    public void deleteUser(UUID userId) {
        log.info("Attempting to delete user with ID: {}", userId);

        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> {
                    log.warn("User not found for deletion with ID: {}. Throwing UserNotFoundException.", userId);
                    return new UserNotFoundException(userId);
                });

        userRepository.deleteById(user.getUserId());
        log.info("User with ID: {} deleted successfully.", userId);
    }

    public void verifyIfUsernameAlreadyInUse(String username) {
        log.debug("Verifying if username '{}' is already in use.", username);
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (userRepository.existsByUsername(username)) {
            log.warn("Username {} already exists. Throwing UserAlreadyExistsException.", username);
            throw new UserAlreadyExistsException(username);
        }
    }
}