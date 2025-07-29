package dev.senna.service;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateItemRequestDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.controller.dto.response.*;
import dev.senna.exception.ClientNotFoundException;
import dev.senna.exception.ItemNotFoundException;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import java.util.List;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ClientRepository clientRepository;

    public OrderEntity createOrder( @Valid CreateOrderReqDto reqDto) {

        var client = clientRepository.findByIdOptional(reqDto.clientId())
                .orElseThrow(() -> new ClientNotFoundException(reqDto.clientId()));

        OrderEntity order = new OrderEntity();
        order.setSaleDate(reqDto.saleDate());
        order.setDeliveryDate(reqDto.deliveryDate());
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
                        orderEntity.getItems().stream().map(item -> new ListItemProductionLineResponse(
                                item.getName(),
                                item.getQuantity(),
                                item.getSaleQuantity(),
                                item.getMaterial(),
                                item.getImage(),
                                item.getStatus(),
                                item.getOrder() != null ? item.getOrder().getId() : null
                        )).toList()
                )).toList();
    }

    public List<LastSendOrdersResponseDto> listLastSendOrders(Integer page, Integer pageSize) {

        var orderQuery = orderRepository.findLastSent(page, pageSize);

        var orderEntities = orderQuery.list();

        var orderDtos = orderEntities.stream()
                .map(orderEntity -> new LastSendOrdersResponseDto(
                        orderEntity.getClient().getClientName(),
                        orderEntity.getItems().stream().map(itemEntity -> new ListItemProductionLineResponse(
                                itemEntity.getName(),
                                itemEntity.getQuantity(),
                                itemEntity.getSaleQuantity(),
                                itemEntity.getMaterial(),
                                itemEntity.getImage(),
                                itemEntity.getStatus(),
                                itemEntity.getOrder().getId()
                        )).toList(),
                        orderEntity.getPostedDate()
                )).toList();

        return orderDtos;

    }

    @Transactional
    public OrderEntity updateOrder(Long orderId, @Valid UpdateOrderReqDto reqDto) {

        var orderToBeUpdated = orderRepository.findByIdOptional(orderId)
                .orElseThrow(() -> new dev.senna.exception.OrderNotFoundException(orderId));

        if (reqDto.clientId() != null) {
            var client = clientRepository.findByIdOptional(reqDto.clientId())
                    .orElseThrow(() -> new ClientNotFoundException(reqDto.clientId()));
            orderToBeUpdated.setClient(client);
        }

        orderToBeUpdated.setStatus(reqDto.status());

        orderRepository.persist(orderToBeUpdated);

        return orderToBeUpdated;
    }

//    public List<ItemEntity> itemEntityMapper(List<UpdateItemRequestDto> items, OrderEntity orderToBeUpdated) {
//       return items.stream()
//                .map(itemDto -> {
//                    ItemEntity newItem = new ItemEntity();
//                    newItem.setName(itemDto.name());
//                    newItem.setQuantity(itemDto.quantity());
//                    newItem.setSaleQuantity(itemDto.saleQuantity());
//                    newItem.setMaterial(itemDto.material());
//                    newItem.setImage(itemDto.image());
//                    newItem.setStatus(itemDto.itemStatus());
//
//                    newItem.setOrder(orderToBeUpdated);
//
//                    return newItem;
//                }).toList();
//    }

}
