package dev.senna.controller.dto;

import java.time.LocalDate;

public record ListOrdersResponseDto(
        LocalDate saleDate,
        LocalDate deliveryDate,
        boolean posted,
        String clientName
) {
}
