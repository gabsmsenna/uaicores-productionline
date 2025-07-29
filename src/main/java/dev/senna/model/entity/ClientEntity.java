package dev.senna.model.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_client")
public class ClientEntity {

    public ClientEntity() {

    }

    public ClientEntity(UUID clientId, String clientName) {
        this.clientId = clientId;
        this.clientName = clientName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID clientId;

    @Column(name = "client_name")
    private String clientName;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderEntity> ordesList;

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<OrderEntity> getOrdesList() {
        return ordesList;
    }


}
