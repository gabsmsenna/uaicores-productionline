package dev.senna.controller.dto.request;

import dev.senna.model.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AddItemRequestDto(

        @NotBlank
        String name,

        @NotNull
        Integer saleQuantity,

        @NotBlank
        String material,

        @NotBlank
        String image,

        Status status,

        Long orderId
) {}
