package dev.senna.controller.dto.response;

public record GetUserByIdResponse(
        String username,
        String role
) {
}
