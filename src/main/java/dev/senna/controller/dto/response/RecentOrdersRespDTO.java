package dev.senna.controller.dto.response;

import dev.senna.model.enums.OrderStatus;

import java.util.List;

public record RecentOrdersRespDTO(
        Long orderId,
        String clientName,
        OrderStatus status,
        List<ItemResponseDto> items
) {
}
