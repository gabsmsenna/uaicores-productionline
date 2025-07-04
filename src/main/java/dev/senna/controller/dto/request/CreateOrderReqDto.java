package dev.senna.controller.dto.request;

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
