package dev.senna.service;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.exception.ClientAlreadyExistsException;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.UserEntity;
import dev.senna.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @InjectMocks
    ClientService clientService;

    @Captor
    private ArgumentCaptor<ClientEntity> clientCaptor;

    @Mock
    private ClientRepository clientRepository;

    @Nested
    class createClient {

        @Test
        @DisplayName("Should persist and return the clientId when client does not exists")
        void shouldReturnClientIdWhenClientDoesNotExists() {

            // Arrange
            String clientName = "CLIENT_NAME";
            CreateClientReqDto requestDto = new CreateClientReqDto(clientName);
            doReturn(false).when(clientRepository).findByUserName(clientName);

            doAnswer(invocation -> {
                ClientEntity entity = invocation.getArgument(0);
                entity.setClientId(UUID.randomUUID());
                return null;
            }).when(clientRepository).persist(any(ClientEntity.class));

            // Act
            UUID clientId = clientService.createClient(requestDto);

            // Assert
            assertNotNull(clientId);
            verify(clientRepository, times(1)).findByUserName(requestDto.clientName());
            verify(clientRepository, times(1)).persist(clientCaptor.capture());
            var clientCaptured = clientCaptor.getValue();
            assertEquals(clientName, clientCaptured.getClientName());

        }

        @Test
        @DisplayName("Should return true and throw exception when the client exists")
        void shouldReturnTrueWhenTheClientExists() {

            // Arrange
            String clientName = "CLIENT_NAME";
            CreateClientReqDto requestDto = new CreateClientReqDto(clientName);
            doReturn(true).when(clientRepository).findByUserName(clientName);

            // Act and Assert
            var exception = assertThrows(ClientAlreadyExistsException.class, () -> {
                clientService.createClient(requestDto);
            });

            // Assert
            assertEquals("Client (" + clientName + ") already exists on database", exception.getDetail());
            verify(clientRepository, times(1)).findByUserName(requestDto.clientName());
            verify(clientRepository, never()).persist(any(ClientEntity.class));
        }
    }
}