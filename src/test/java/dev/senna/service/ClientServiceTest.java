package dev.senna.service;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.model.entity.ClientEntity;
import dev.senna.repository.ClientRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.inject.Inject;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@QuarkusTest
class ClientServiceTest {

    @Inject
    ClientService clientService;

    @InjectMock
    ClientRepository clientRepository;

    @Test
    @DisplayName("Deve criar um cliente e retornar o ID gerado")
    void shouldCreateClientAndReturnGeneratedId() {

        // Arrange
        // 1. Definição dos dados de entrada.
        var clientDto = new CreateClientReqDto("CLIENT_NAME_TEST");
        var expectedId = UUID.randomUUID();

        // 2. Configuração do comportamento do mock.
        // Usamos doAnswer para simular a lógica de persistência que atribui um ID à entidade.
        // Esta é a forma correta de fazer o que você intencionou.
        doAnswer(invocation -> {
            ClientEntity entity = invocation.getArgument(0);
            entity.setClientId(expectedId); // Simula o banco de dados atribuindo o ID.
            return null;
        }).when(clientRepository).persist(any(ClientEntity.class));

        // 3. Preparação do ArgumentCaptor para capturar a entidade enviada ao persist.
        ArgumentCaptor<ClientEntity> clientEntityCaptor = ArgumentCaptor.forClass(ClientEntity.class);

        // Act
        // 4. Execução do método sob teste. Apenas uma chamada.
        UUID returnedId = clientService.createClient(clientDto);

        // Assert
        // 5. Verificação do retorno do método.
        assertNotNull(returnedId, "O ID retornado não deve ser nulo.");
        assertEquals(expectedId, returnedId, "O ID retornado deve ser o mesmo que foi simulado na persistência.");

        // 6. Verificação da interação com o mock e captura do argumento.
        // Garantimos que o método 'persist' foi chamado exatamente uma vez.
        verify(clientRepository).persist(clientEntityCaptor.capture());

        // 7. Verificação dos valores do objeto capturado.
        // Agora podemos inspecionar a entidade que o serviço tentou salvar.
        ClientEntity capturedEntity = clientEntityCaptor.getValue();
        assertNotNull(capturedEntity, "A entidade persistida não deve ser nula.");
        assertEquals(clientDto.clientName(), capturedEntity.getClientName(), "O nome do cliente na entidade não corresponde ao DTO.");
    }
}