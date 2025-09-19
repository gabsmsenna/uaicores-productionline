package dev.senna.controller.dto.request;

import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.Material;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddItemRequestDto(

        @NotBlank
        String name,

        @NotNull
        Integer saleQuantity,

        @NotNull
        Material material,

        @NotBlank
        String image,

        Long orderId
) {}
