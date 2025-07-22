package dev.senna.controller.dto.response;

import dev.senna.model.enums.OrderStatus;

import java.time.LocalDate;

public record ListOrdersResponseDto(
        LocalDate saleDate,
        LocalDate deliveryDate,
        String clientName,
        OrderStatus status
) {
}
