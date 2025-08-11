package dev.senna.service;

import dev.senna.controller.dto.request.CreateClientReqDto;
import dev.senna.controller.dto.request.UpdateClientReqDto;
import dev.senna.controller.dto.response.ClientResDto;
import dev.senna.exception.ClientAlreadyExistsException;
import dev.senna.exception.ClientHasOrdersException;
import dev.senna.exception.ClientNotFoundException;
import dev.senna.model.entity.ClientEntity;
import dev.senna.repository.ClientRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.awt.font.TextHitInfo;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ClientService {

    @Inject
    private ClientRepository clientRepository;

    public UUID createClient(CreateClientReqDto reqDto) {

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientName(reqDto.clientName());

        if (clientRepository.findByUserName(clientEntity.getClientName())) {
            throw new ClientAlreadyExistsException(reqDto.clientName());
        }

        clientRepository.persist(clientEntity);

        return clientEntity.getClientId();
    }

    public ClientResDto findClientById(UUID clientId) {

        var client = clientRepository.findByIdOptional(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        return new ClientResDto(client.getClientId(), client.getClientName());
    }

    public List<ClientResDto> findAllClients(Integer page, Integer pageSize) {

        return clientRepository.findAll()
                .page(Page.of(page, pageSize))
                .list()
                .stream()
                .map(client -> new ClientResDto(client.getClientId(), client.getClientName()))
                .toList();
    }

    @Transactional
    public ClientResDto updateClient(UUID clientId, UpdateClientReqDto reqDto) {
        var clientEntity = clientRepository.findByIdOptional(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (!clientEntity.getClientName().equals(reqDto.clientName()) &&
                clientRepository.findByUserName(reqDto.clientName())) {
            throw new ClientAlreadyExistsException(reqDto.clientName());
        }

        clientEntity.setClientName(reqDto.clientName());
        clientRepository.persist(clientEntity);

        return new ClientResDto(clientEntity.getClientId(), clientEntity.getClientName());
    }

    @Transactional
    public void deleteClient(UUID clientId) {
        var clientEntity = clientRepository.findByIdOptional(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (clientEntity.getOrdersList() != null && !clientEntity.getOrdersList().isEmpty()) {
            throw new ClientHasOrdersException(
                    clientEntity.getClientId(),
                    clientEntity.getClientName()
            );
        }

        clientRepository.delete(clientEntity);
    }

}
