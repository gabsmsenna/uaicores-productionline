package dev.senna.service;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.controller.dto.response.ListUserResponse;
import dev.senna.controller.dto.response.GetUserByIdResponse;
import dev.senna.controller.dto.request.UpdateUserDto;
import dev.senna.controller.dto.response.UpdateUserRespDto;
import dev.senna.exception.UserAlreadyExists;
import dev.senna.exception.UserNotFoundException;
import dev.senna.model.entity.UserEntity;
import dev.senna.model.enums.UserRole;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    public UUID createUser(CreateUserRequest userReq) {
        if (userRepository.existsByUsername(userReq.username())) {
            throw new UserAlreadyExists(userReq.username());
        }

        var user = new UserEntity();
        user.setUsername(userReq.username());
        user.setPassword(BcryptUtil.bcryptHash(userReq.password()));
        user.setRole(UserRole.OFFICER);

        userRepository.persist(user);

        return user.getUserId();
    }

    public List<ListUserResponse> findAll(Integer page, Integer pageSize) {
        var users = userRepository.findAll()
                .page(page, pageSize)
                .list();

        return users.stream()
                .map(userEntity -> new ListUserResponse(
                        userEntity.getUsername(),
                        userEntity.getRole().getRoleName()
                )).toList();
    }

    public GetUserByIdResponse findUserById(UUID id) {

        var user = userRepository.findByIdOptional(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return user.toResponse();
    }

    public UpdateUserRespDto updateUser(UUID userId, UpdateUserDto updateUserRequest) {

        var user =  userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setUsername(updateUserRequest.username());
        user.setPassword(BcryptUtil.bcryptHash(updateUserRequest.password()));
        user.setRole(UserRole.valueOf(updateUserRequest.role()));

        userRepository.persist(user);

        return new UpdateUserRespDto(user.getUsername(), user.getPassword(), user.getRole());
    }

    public void deleteUser(UUID userId) {

        var user = userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.deleteById(user.getUserId());
    }
}
