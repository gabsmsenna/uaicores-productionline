package dev.senna.controller.dto;

public record GetUserByIdResponse(
        String username,
        String role
) {
}
