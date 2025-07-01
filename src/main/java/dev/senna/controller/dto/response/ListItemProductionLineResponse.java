package dev.senna.controller.dto.response;

import dev.senna.model.enums.ItemStatus;

public record ListItemProductionLineResponse(
        String name,
        Integer quantity,
        Integer saleQuantity,
        String material,
        String image,
        ItemStatus itemStatus,
        Long orderId
) {
}
