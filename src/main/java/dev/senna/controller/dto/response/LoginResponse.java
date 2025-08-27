package dev.senna.controller.dto.response;

public record LoginResponse(
        String token,
        String username,
        String role
) {
}
