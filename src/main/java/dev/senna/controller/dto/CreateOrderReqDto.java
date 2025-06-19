package dev.senna.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateOrderReqDto(

        @NotNull
        LocalDate saleDate,

        @NotNull
        LocalDate deliveryDate,

        UUID clientId
) {
}
