package dev.senna.controller.dto;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;

public record ListProductionLineResponse(
        String name,
        Integer quantity,
        Integer saleQuantity,
        String material,
        String image,
        ItemStatus itemStatus,
        Long orderId
) {
}
