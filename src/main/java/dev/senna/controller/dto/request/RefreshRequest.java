package dev.senna.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank  String refreshToken
) {
}
