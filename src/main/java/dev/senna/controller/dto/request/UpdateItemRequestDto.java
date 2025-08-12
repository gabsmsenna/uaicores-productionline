package dev.senna.controller.dto.request;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.Material;

public record UpdateItemRequestDto(
    String name,
    Integer quantity,
    Integer saleQuantity,
    Material material,
    String image,
    ItemStatus itemStatus,
    Long orderId
) {
}
