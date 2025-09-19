package dev.senna.controller.dto.response;

import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.Material;

public record ItemResponseDto(
        Long id,
        String name,
        Integer quantity,
        Integer saleQuantity,
        Material material,
        String image,
        ItemStatus status
) {
}
