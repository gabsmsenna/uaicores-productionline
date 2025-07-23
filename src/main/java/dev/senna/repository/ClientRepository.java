package dev.senna.repository;

import dev.senna.model.entity.ClientEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ClientRepository implements PanacheRepositoryBase<ClientEntity, UUID> {
}
