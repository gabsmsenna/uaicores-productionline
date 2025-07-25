package dev.senna.service;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.model.entity.ClientEntity;
import dev.senna.repository.ClientRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@QuarkusTest
class ClientServiceTest {

    @Inject
    ClientService clientService;

    @InjectMock
    private ClientRepository clientRepository;


    @Test
    void createClient() {

        // 1. Cenário (Arrange)
        String clientName = "John Doe";
        CreateClientReqDto requestDto = new CreateClientReqDto(clientName);

        // Captura o argumento passado para o método persist
        ArgumentCaptor<ClientEntity> clientEntityCaptor = ArgumentCaptor.forClass(ClientEntity.class);

        // Simula o comportamento do método persist para definir um ID no cliente
        doAnswer(invocation -> {
            ClientEntity entity = invocation.getArgument(0);
            entity.setClientId(UUID.randomUUID()); // Simula a geração do ID pelo banco de dados
            return null;
        }).when(clientRepository).persist(any(ClientEntity.class));

        // 2. Ação (Act)
        UUID newClientId = clientService.createClient(requestDto);

        // 3. Verificação (Assert)
        assertNotNull(newClientId, "O ID do cliente não deveria ser nulo.");

        // Verifica se o método persist foi chamado exatamente uma vez
        verify(clientRepository).persist(clientEntityCaptor.capture());

        // Captura a entidade que foi "salva"
        ClientEntity persistedClient = clientEntityCaptor.getValue();

        // Verifica se o nome do cliente na entidade está correto
        assertEquals(clientName, persistedClient.getClientName(), "O nome do cliente na entidade persistida está incorreto.");

    }
}