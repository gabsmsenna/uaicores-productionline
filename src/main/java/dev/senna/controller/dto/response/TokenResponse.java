package dev.senna.controller.dto.response;

public record TokenResponse(String accessToken, long expiresIn, String refreshToken) {
}
