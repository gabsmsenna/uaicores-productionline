package dev.senna.controller.dto.request;

import dev.senna.model.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public record UpdateOrderReqDto(
        OrderStatus status,
        UUID clientId
) {
}
