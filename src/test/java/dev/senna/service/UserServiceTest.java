package dev.senna.service;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.controller.dto.request.UpdateUserDto;
import dev.senna.exception.UserAlreadyExistsException;
import dev.senna.exception.UserNotFoundException;
import dev.senna.model.entity.UserEntity;
import dev.senna.model.enums.UserRole;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("createUser() tests")
    class createUserTest {

        @Test
        @DisplayName("Should create a user when the username does not exists")
        void testCreateUser() {
            when(userRepository.existsByUsername(anyString())).thenReturn(false);

            doAnswer(invocation -> {
                UserEntity userEntity = invocation.getArgument(0);
                userEntity.setUserId(UUID.randomUUID());
                return null;
            }).when(userRepository).persist(any(UserEntity.class));

            var userRequest = new CreateUserRequest("USER_NAME", "PASSWORD");
            var userId = userService.createUser(userRequest);

            assertNotNull(userId);
            verify(userRepository).persist(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw an exception when the username already exists")
        void testCreateUser_whenUserAlreadyExists() {
            when(userRepository.existsByUsername(anyString())).thenReturn(true);
            var userRequest = new CreateUserRequest("testuser", "password");

            assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userRequest));
        }
    }

    @Nested
    @DisplayName("findAll() tests")
    class findAllTest {

        @Test
        @DisplayName("Should return a list of users")
        void testFindAll() {

            var user = new UserEntity();
            user.setUsername("USERNAME");
            user.setRole(UserRole.OFFICER);

            var mockQuery = mock(PanacheQuery.class);

            when(mockQuery.page(anyInt(), anyInt())).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(Collections.singletonList(user));
            when(userRepository.findAll()).thenReturn(mockQuery);

            var users = userService.findAll(0, 10);

            assertFalse(users.isEmpty());
            assertEquals("USERNAME", users.get(0).userName());
        }
    }

    @Nested
    @DisplayName("findUserById() tests")
    class findUserByIdTest {

        @Test
        @DisplayName("Should return an user matching by id")
        void shouldReturnAnUserById() {

            var userId = UUID.randomUUID();
            var user = new UserEntity();

            user.setUserId(userId);
            user.setUsername("USERNAME");
            user.setRole(UserRole.OFFICER);

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));

            var userResponse = userService.findUserById(userId);

            assertNotNull(userResponse);
            assertEquals("USERNAME", userResponse.username());
            verify(userRepository).findByIdOptional(userId);
        }

        @Test
        @DisplayName("Should throw an exception when the user is not found by id")
        void testFindUserById_whenUserNotFound() {
            var userId = UUID.randomUUID();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            var exception = assertThrows(UserNotFoundException.class, () -> userService.findUserById(userId));

            verify(userRepository).findByIdOptional(userId);
            assertEquals("User not found on the system! UserID : " + userId, exception.getDetail());
        }
    }

    @Nested
    @DisplayName("updateUser() tests")
    class updateUserTest {

        @Test
        @DisplayName("Should update an user successfully")
        void testUpdateUser() {

            var userId = UUID.randomUUID();
            var existingUser = new UserEntity();
            existingUser.setUserId(userId);
            existingUser.setUsername("USERNAME");
            existingUser.setPassword("PASSWD");
            existingUser.setRole(UserRole.OFFICER);

            var updateUserDto = new UpdateUserDto("UPDATE_NAME", "UPDATE_PASSWD");

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(existingUser));

            userService.updateUser(userId, updateUserDto);

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);

            verify(userRepository).persist(userCaptor.capture());

            UserEntity capturedUser = userCaptor.getValue();

            assertNotNull(capturedUser);
            assertEquals(updateUserDto.username(), capturedUser.getUsername());
            assertTrue(BcryptUtil.matches(updateUserDto.password(), capturedUser.getPassword()));
        }

        @Test
        @DisplayName("Should throw an exception when the user is not found by id")
        void testUpdateUser_whenUserNotFound() {

            var userId = UUID.randomUUID();

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            var updateUserDto = new UpdateUserDto("UPDATE_NAME", "UPDATE_PASSWD");

            var exception = assertThrows(UserNotFoundException.class, () -> {
                userService.updateUser(userId, updateUserDto);
            });

            assertTrue(exception.getMessage().contains(userId.toString()));
            verify(userRepository).findByIdOptional(userId);
            verify(userRepository, never()).persist(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("deleteUser() tests")
    class deleteUserTest {

        @Test
        @DisplayName("Should delete an user successfully bi his id")
        void shouldDeleteAnUserSuccessfully() {

            var userId = UUID.randomUUID();
            var user = new UserEntity();
            user.setUserId(userId);

            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));
            when(userRepository.deleteById(any(UUID.class))).thenReturn(true);

            userService.deleteUser(userId);

            verify(userRepository).deleteById(userId);
        }

        @Test
        @DisplayName("Should throw an exception when the user is not found by id")
        void testDeleteUser_whenUserNotFound() {
            var userId = UUID.randomUUID();
            when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        }
    }




}
