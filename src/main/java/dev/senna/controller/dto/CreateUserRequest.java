package dev.senna.controller.dto;

public record CreateUserRequest(
        String username,
        String password
) {
}
