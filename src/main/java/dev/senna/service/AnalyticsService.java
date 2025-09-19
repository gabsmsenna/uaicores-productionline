package dev.senna.service;

import dev.senna.controller.dto.response.DashboardAnalyticsResponseDTO;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AnalyticsService {

    @Inject
    ItemRepository itemRepository;

    @Inject
    ItemService itemService;

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderService orderService;


    public DashboardAnalyticsResponseDTO getDashboardAnalyticsService() {
        List<ItemStatus> allowedItemStatus = List.of(
                ItemStatus.IMPRESSO,
                ItemStatus.ENCARTELADO,
                ItemStatus.EM_SILK,
                ItemStatus.CHAPADO,
                ItemStatus.VERSO_PRONTO,
                ItemStatus.ACABAMENTO
        );

        var ordersInProduction = orderRepository.find("status in ?1", OrderStatus.PRODUCAO).stream().count();

        var ordersWaitingShipping = orderRepository.find("status in ?1", OrderStatus.FINALIZADO).stream().count();

        var itemsInProduction = itemRepository.find("itemStatus in ?1", allowedItemStatus).stream().count();

        var ordersShippedLastWeek = countOrdersPostedLastWeek();

        return new DashboardAnalyticsResponseDTO(ordersInProduction, ordersWaitingShipping, itemsInProduction, ordersShippedLastWeek);

    }

    public List<OrderEntity> getOrdersShippedLastWeek() {
        LocalDate today = LocalDate.now();

        var startOfLastWeek = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        var endOfLastWeek = today.minusWeeks(1).with(DayOfWeek.SUNDAY);

        return orderRepository.findByPostedDateBetween(startOfLastWeek, endOfLastWeek);
    }

    public long countOrdersPostedLastWeek() {
        LocalDate today = LocalDate.now();

        LocalDate startOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfLastWeek = today.minusWeeks(1).with(java.time.DayOfWeek.SUNDAY);

        return orderRepository.countByPostedDateBetween(startOfLastWeek, endOfLastWeek);
    }
}
