package dev.senna.controller.dto.response;

import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public record UpdateOrderResDto(
        List<ItemEntity> items,
        OrderStatus status,
        UUID clientId
) {

    public UpdateOrderResDto(OrderEntity entity) {
        this(
                entity.getItems(),
                entity.getStatus(),
                entity.getClient().getClientId()
        );
    }
}
