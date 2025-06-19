package dev.senna.service;

import dev.senna.controller.dto.CreateOrderReqDto;
import dev.senna.model.entity.OrderEntity;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;

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
}
