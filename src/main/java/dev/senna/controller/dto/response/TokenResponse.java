package dev.senna.controller.dto.response;

import dev.senna.model.enums.UserRole;

public record TokenResponse(
        String accessToken,
        long expiresIn,
        String refreshToken,
        UserRole userRole) {
}
