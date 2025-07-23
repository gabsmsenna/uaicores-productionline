package dev.senna.controller.dto.request;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;

public record UpdateItemRequestDto(
    String name,
    Integer quantity,
    Integer saleQuantity,
    String material,
    String image,
    ItemStatus itemStatus,
    Long orderId
) {
}
