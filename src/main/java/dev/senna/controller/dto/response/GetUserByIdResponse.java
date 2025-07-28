package dev.senna.controller.dto.response;

import dev.senna.model.enums.UserRole;

public record GetUserByIdResponse(
        String username,
        UserRole role
) {
}
