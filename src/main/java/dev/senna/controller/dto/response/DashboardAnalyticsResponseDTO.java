package dev.senna.controller.dto.response;

public record DashboardAnalyticsResponseDTO(
        long ordersInProduction,
        long ordersWaitingShipping,
        long itemsInProduction,
        long ordersShippedLastWeek
) {
}
