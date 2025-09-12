package dev.senna.model.entity;

import dev.senna.controller.dto.response.GetUserByIdResponse;
import dev.senna.model.enums.UserRole;
import io.quarkus.security.User;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "tb_users")
@UserDefinition
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Username
    @NotBlank
    @Column(unique = true)
    private String username;

    @Password
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    @NotNull
    private UserRole role;

    public UserEntity(UUID userId, String username, String password, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UserEntity() {}

    public GetUserByIdResponse toResponse() {
        return new GetUserByIdResponse(
                this.username,
                this.role
        );
    }

    @Roles
    public String getRoles() {
        return this.role != null ? role.name() : null;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
