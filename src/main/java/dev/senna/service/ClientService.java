package dev.senna.service;

import dev.senna.controller.dto.CreateClientReqDto;
import dev.senna.model.entity.ClientEntity;
import dev.senna.repository.ClientRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class ClientService {

    @Inject
    private ClientRepository clientRepository;

    public UUID createClient(CreateClientReqDto reqDto) {

        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setClientName(reqDto.clientName());

        clientRepository.persist(clientEntity);

        return clientEntity.getClientId();
    }
}
