package dev.senna.controller.dto.request;

import dev.senna.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Password cannot be blank")
        String password,

        @NotNull(message = "User role cannot be blank")
        UserRole role
) {
}
