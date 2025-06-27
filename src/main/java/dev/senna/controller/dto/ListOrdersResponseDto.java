package dev.senna.controller.dto;

import dev.senna.model.enums.OrderStatus;

import java.time.LocalDate;

public record ListOrdersResponseDto(
        LocalDate saleDate,
        LocalDate deliveryDate,
        boolean posted,
        String clientName,
        OrderStatus status
) {
}
