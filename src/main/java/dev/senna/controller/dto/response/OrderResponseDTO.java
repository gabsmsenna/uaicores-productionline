package dev.senna.controller.dto.response;

import dev.senna.model.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public record OrderResponseDTO(
        Long orderId,
        LocalDate saleDate,
        LocalDate deliveryDate,
        String clientName,
        OrderStatus status,
        List<ItemResponseDto> items
) {
}
