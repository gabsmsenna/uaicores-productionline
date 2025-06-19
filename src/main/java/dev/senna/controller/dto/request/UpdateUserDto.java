package dev.senna.controller.dto.request;

public record UpdateUserDto(
        String username,
        String password,
        String role
) {
}
