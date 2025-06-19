package dev.senna.controller.dto.request;

public record CreateUserRequest(
        String username,
        String password
) {
}
