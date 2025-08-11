package dev.senna.controller.dto.response;

import dev.senna.model.enums.UserRole;

public record UpdateUserRespDto(
        String username,
        String password,
        UserRole role
) {
}
