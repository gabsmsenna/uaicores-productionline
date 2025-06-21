package dev.senna.controller.dto;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.Status;

public record ListProductionLineResponse(
        String name,
        Integer quantity,
        Integer saleQuantity,
        String material,
        String image,
        Status status,
        OrderEntity order
) {
}
