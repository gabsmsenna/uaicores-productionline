package dev.senna.controller.dto;

public record UpdateUserDto(
        String username,
        String password,
        String role
) {
}
