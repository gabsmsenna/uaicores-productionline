package dev.senna.service;

import dev.senna.controller.dto.CreateOrderReqDto;
import dev.senna.controller.dto.ListOrdersResponseDto;
import dev.senna.controller.dto.response.ItemResponseDto;
import dev.senna.controller.dto.response.ListOrderProductionResponseDto;

import dev.senna.controller.dto.response.ListOrderProductionResponseDto;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.awt.print.Pageable;
import java.util.List;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ClientRepository clientRepository;

    public OrderEntity createOrder( @Valid CreateOrderReqDto reqDto) {

        var client = clientRepository.findById(reqDto.clientId());

        OrderEntity order = new OrderEntity();
        order.setSaleDate(reqDto.saleDate());
        order.setDeliveryDate(reqDto.deliveryDate());
        order.setPosted(false);
        order.setClient(client);
        order.setStatus(OrderStatus.PRODUCAO);

        orderRepository.persist(order);

        return order;
    }

    public List<ListOrdersResponseDto> listOrders(Integer page, Integer pageSize) {

        var orders = orderRepository.findAll()
                .page(Page.of(page, pageSize))
                .list();

        return orders.stream()
                .map(itemEntity -> new ListOrdersResponseDto(
                        itemEntity.getSaleDate(),
                        itemEntity.getDeliveryDate(),
                        itemEntity.isPosted(),
                        itemEntity.getClient().getClientName(),
                        itemEntity.getStatus()
                )).toList();
    }

    public List<ListOrdersResponseDto> listOrdersInProduction(Integer page, Integer pageSize) {
        var orders = orderRepository.listOrdersInProduction(page, pageSize);

        return orders.stream()
                .map(orderEntity -> new ListOrdersResponseDto(
                        orderEntity.getSaleDate(),
                        orderEntity.getDeliveryDate(),
                        orderEntity.isPosted(),
                        orderEntity.getClient().getClientName(),
                        orderEntity.getStatus()
                )).toList();
    }

    public List<ListOrderProductionResponseDto> listProduction(Integer page, Integer pageSize) {
        var orders = orderRepository.find("status", OrderStatus.PRODUCAO).page(Page.of(page, pageSize)).list();

        return orders.stream()
                .map(orderEntity -> new ListOrderProductionResponseDto(
                        orderEntity.getClient().getClientName(),
                        orderEntity.getStatus(),
                        orderEntity.getItems().stream().map(item -> new ItemResponseDto(
                                item.getId(),
                                item.getName(),
                                item.getQuantity(),
                                item.getSaleQuantity(),
                                item.getMaterial(),
                                item.getImage(),
                                item.getStatus()
                        )).toList()
                )).toList();
    }
}
