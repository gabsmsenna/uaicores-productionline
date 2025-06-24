package dev.senna.service;

import dev.senna.controller.dto.CreateOrderReqDto;
import dev.senna.controller.dto.ListOrdersResponseDto;
import dev.senna.model.entity.OrderEntity;
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
                        itemEntity.getClient().getClientName()
                )).toList();
    }
}
