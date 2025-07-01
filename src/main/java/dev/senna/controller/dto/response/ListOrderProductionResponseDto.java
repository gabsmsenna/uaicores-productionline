package dev.senna.controller.dto.response;

import dev.senna.model.enums.OrderStatus;

import java.util.List;

public record ListOrderProductionResponseDto(
        String clientName,
        OrderStatus status,
        List<ListItemProductionLineResponse> items
) {
}
