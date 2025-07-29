package dev.senna.service;

import dev.senna.controller.dto.request.CreateUserRequest;
import dev.senna.exception.UserAlreadyExists;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.UserEntity;
import dev.senna.model.enums.UserRole;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PanacheQuery<UserEntity> panacheMock;

    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    @Nested
    @DisplayName("createUser() tests")
    class createUser {

        @Test
        @DisplayName("Should create an user successfully")
        void shouldCreateAnUserSuccessfully() {

            // Arrange
            var createUserRequest = new CreateUserRequest("USERNAME", "PASSWORD");
            var idGenerated = UUID.randomUUID();
            when(userRepository.existsByUsername("USERNAME")).thenReturn(false);

            doAnswer(invocationOnMock -> {
                UserEntity user = invocationOnMock.getArgument(0);
                user.setUserId(idGenerated);
                return null;
            }).when(userRepository).persist(any(UserEntity.class));

            // Act
            var clientId = userService.createUser(createUserRequest);

            // Assert
            verify(userRepository, times(1)).persist(userCaptor.capture());
            var persistedUser = userCaptor.getValue();

            assertNotNull(clientId);
            assertEquals(idGenerated, clientId);
            assertEquals(createUserRequest.username(), persistedUser.getUsername());

            assertNotEquals(createUserRequest.password(), persistedUser.getPassword());
            assertTrue(BcryptUtil.matches(createUserRequest.password(), persistedUser.getPassword()));
        }

        @Test
        @DisplayName("Should throw UserAlreadyExists when username already exists on database")
        void shouldThrowUserAlreadyExistsWhenUsernameAlreadyExists() {

            // Arrange
            var reqDto = new CreateUserRequest("USERNAME", "PASSWORD");
            when(userRepository.existsByUsername(reqDto.username())).thenReturn(true);

            // Act and Assert
            var exception = assertThrows(UserAlreadyExists.class, () -> {
                userService.createUser(reqDto);
            });

            assertEquals("User with username " + reqDto.username() + " already exists in the system", exception.getDetail());
            verify(userRepository, times(1)).existsByUsername(reqDto.username());
            verify(userRepository, never()).persist(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("findAll() tests")
    class findAll {

        @Test
        @DisplayName("Should return a list with all users in the system")
        void shouldReturnAListWithAllUsers() {

            // Arrange
            var user1 = new UserEntity(UUID.randomUUID(), "USERNAME", "PASSWORD", UserRole.OFFICER);
            var user2 = new UserEntity(UUID.randomUUID(), "USERNAME_2", "PASSWORD_2", UserRole.OFFICER);

            when(userRepository.findAll()).thenReturn(panacheMock);
            when(panacheMock.page(0, 10)).thenReturn(panacheMock);
            when(panacheMock.list()).thenReturn(List.of(user1, user2));

            // Act
            var resultList = userService.findAll(0, 10);

            // Assert
            assertEquals(2, resultList.size());
            assertEquals(user1.getUsername(), resultList.getFirst().userName());
            assertEquals(user2.getUsername(), resultList.getLast().userName());
            assertEquals(user1.getRole().getRoleName(), resultList.getFirst().role());
            assertEquals(user2.getRole().getRoleName(), resultList.getLast().role());

        }
    }
}