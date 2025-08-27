package dev.senna.model.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Table(name = "tb_refresh_token")
public class RefreshTokenEntity extends PanacheEntityBase {

    @Id
    @Column(name = "token_id")
    public UUID tokenId;

    @Column(nullable = false)
    public UUID userId;

    @Column(nullable = false, unique = true)
    public String tokenHash; // hash do refresh token opaco

    @Column(nullable = false)
    public Instant expiresAt;

    @Column(nullable = false)
    public boolean revoked;
}
