package dev.senna.controller.dto.request;

import dev.senna.model.enums.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddItemRequestDto(

        @NotBlank
        String name,

        @NotNull
        Integer saleQuantity,

        @NotBlank
        String material,

        @NotBlank
        String image,

        ItemStatus itemStatus,

        Long orderId
) {}
