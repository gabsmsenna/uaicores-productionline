package dev.senna.service;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.controller.dto.response.*;
import dev.senna.exception.*;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;

    @Inject
    private ClientRepository clientRepository;

    @Inject
    SecurityIdentity identity;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    // Constantes para melhor manutenibilidade
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Transactional
    public Long createOrder(@Valid CreateOrderReqDto reqDto) throws OrderServiceException {
        log.info("Iniciando criação de pedido para clienteId: {}", reqDto.clientId());

            validateDatesForCreation(reqDto.saleDate(), reqDto.deliveryDate());

            var client = clientRepository.findByIdOptional(reqDto.clientId())
                    .orElseThrow(() -> {
                        log.error("Cliente não encontrado com ID: {}", reqDto.clientId());
                        return new ClientNotFoundException(reqDto.clientId());
                    });

            log.debug("Cliente encontrado: {} (ID: {})", client.getClientName(), client.getClientId());

            OrderEntity order = new OrderEntity();
            order.setSaleDate(reqDto.saleDate());
            order.setDeliveryDate(reqDto.deliveryDate());
            order.setClient(client);
            order.setStatus(OrderStatus.PRODUCAO);

            orderRepository.persist(order);

            log.info("Pedido criado com sucesso - ID: {}, Cliente: {}, Status: {}",
                    order.getId(), client.getClientName(), OrderStatus.PRODUCAO);

            return order.getId();
    }

    public List<ListOrdersResponseDto> listOrders(OrderStatus status, UUID clientId, Integer page, Integer pageSize) {

        int validatedPage = validatePage(page);
        int validatedPageSize = validatePageSize(pageSize);

        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM OrderEntity o JOIN FETCH o.client c WHERE 1=1");
        Parameters param = new Parameters();

        if (status != null) {
            queryBuilder.append(" AND o.status = :status");
            param.and("status", status);
        }

        if (clientId != null) {
            queryBuilder.append(" AND c.clientId = :clientId");
            param.and("clientId", clientId);
        }

        var orders = orderRepository.find(queryBuilder.toString(), Sort.by("saleDate").descending(), param)
                .page(Page.of(validatedPage, validatedPageSize))
                .list();

        return orders.stream()
                .map(order -> new ListOrdersResponseDto(order.getSaleDate(),
                        order.getDeliveryDate(),
                        order.getClient().getClientName(),
                        order.getStatus()))
                        .toList();
    }

    public List<ListOrderProductionResponseDto> listProduction(Integer page, Integer pageSize) {
        int validatedPage = validatePage(page);
        int validatedPageSize = validatePageSize(pageSize);

        log.debug("Listing actual production line - page: {}, size: {}", validatedPage, validatedPageSize);

        try {
            var orders = orderRepository.find("status", OrderStatus.PRODUCAO)
                    .page(Page.of(validatedPage, validatedPageSize))
                    .list();

            log.info("Encontrados {} pedidos em produção para listagem detalhada na página {}",
                    orders.size(), validatedPage);

            return orders.stream()
                    .map(orderEntity -> {
                        var itemsCount = orderEntity.getItems().size();
                        log.debug("Pedido ID: {} tem {} itens", orderEntity.getId(), itemsCount);

                        return new ListOrderProductionResponseDto(
                                orderEntity.getClient().getClientName(),
                                orderEntity.getStatus(),
                                orderEntity.getItems().stream().map(item -> new ListItemProductionLineResponse(
                                        item.getName(),
                                        item.getQuantity(),
                                        item.getSaleQuantity(),
                                        item.getMaterial(),
                                        item.getImage(),
                                        item.getStatus(),
                                        Optional.ofNullable(item.getOrder())
                                                .map(OrderEntity::getId)
                                                .orElse(null)
                                )).toList()
                        );
                    }).toList();

        } catch (Exception e) {
            log.error("Erro ao listar produção - página: {}, tamanho: {} - Erro: {}",
                    validatedPage, validatedPageSize, e.getMessage(), e);
            throw e;
        }
    }

    public List<LastSendOrdersResponseDto> listLastSendOrders(Integer page, Integer pageSize) {
        int validatedPage = validatePage(page);
        int validatedPageSize = validatePageSize(pageSize);

        log.debug("Listando últimos pedidos enviados - página: {}, tamanho: {}", validatedPage, validatedPageSize);

        try {
            var orderQuery = orderRepository.findLastSent(validatedPage, validatedPageSize);
            var orderEntities = orderQuery.list();

            log.info("Encontrados {} pedidos enviados na página {}", orderEntities.size(), validatedPage);

            var orderDtos = orderEntities.stream()
                    .map(orderEntity -> {
                        log.debug("Processando pedido enviado ID: {} - Data de postagem: {}",
                                orderEntity.getId(), orderEntity.getPostedDate());

                        return new LastSendOrdersResponseDto(
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
                        );
                    }).toList();

            return orderDtos;

        } catch (Exception e) {
            log.error("Erro ao listar últimos pedidos enviados - página: {}, tamanho: {} - Erro: {}",
                    validatedPage, validatedPageSize, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public UpdateOrderResDto updateOrder(Long orderId, @Valid UpdateOrderReqDto reqDto) {
        log.info("Iniciando atualização do pedido ID: {} - Novos Parâmetros - Status: {} - Sale Date: {} - Delivery Date: {} - Client ID: {} ",
                orderId, reqDto.status(), reqDto.saleDate(), reqDto.deliveryDate(), reqDto.clientId());

        try {
            var orderToBeUpdated = orderRepository.findByIdOptional(orderId)
                    .orElseThrow(() -> {
                        log.error("Pedido não encontrado com ID: {}", orderId);
                        return new dev.senna.exception.OrderNotFoundException(orderId);
                    });

            log.debug("Pedido encontrado - ID: {}, Status atual: {}, Cliente: {}",
                    orderToBeUpdated.getId(), orderToBeUpdated.getStatus(),
                    orderToBeUpdated.getClient().getClientName());

            boolean isAdmin = identity.getRoles().contains("ADMIN");
            boolean isOfficer = identity.getRoles().contains("OFFICER");

            if (isOfficer) {
                if (reqDto.clientId() != null || reqDto.saleDate() != null || reqDto.deliveryDate() != null) {
                    log.warn("Usuário {} com role OFFICER tentou alterar campos não permitidos no pedido {}",
                            identity.getPrincipal().getName(), orderId);
                    throw new ForbiddenException("OFFICER pode atualizar apenas o status do pedido.");
                }
            }

            // Atualização do cliente (apenas ADMIN)
            if (isAdmin && reqDto.clientId() != null && !reqDto.clientId().equals(orderToBeUpdated.getClient().getClientId())) {
                var client = clientRepository.findByIdOptional(reqDto.clientId())
                        .orElseThrow(() -> {
                            log.error("Cliente não encontrado com ID: {}", reqDto.clientId());
                            return new ClientNotFoundException(reqDto.clientId());
                        });

                log.info("Alterando cliente do pedido {} de '{}' para '{}'",
                        orderId, orderToBeUpdated.getClient().getClientName(), client.getClientName());
                orderToBeUpdated.setClient(client);
            }

            // Atualização das datas (apenas ADMIN)
            if (isAdmin && (reqDto.saleDate() != null || reqDto.deliveryDate() != null)) {
                LocalDate saleDate = reqDto.saleDate() != null ? reqDto.saleDate() : orderToBeUpdated.getSaleDate();
                LocalDate deliveryDate = reqDto.deliveryDate() != null ? reqDto.deliveryDate() : orderToBeUpdated.getDeliveryDate();

                validateDatesForUpdate(saleDate, deliveryDate, orderToBeUpdated);

                if (reqDto.saleDate() != null) {
                    log.debug("Atualizando data de venda do pedido {} para {}", orderId, reqDto.saleDate());
                    orderToBeUpdated.setSaleDate(reqDto.saleDate());
                }

                if (reqDto.deliveryDate() != null) {
                    log.debug("Atualizando data de entrega do pedido {} para {}", orderId, reqDto.deliveryDate());
                    orderToBeUpdated.setDeliveryDate(reqDto.deliveryDate());
                }
            }

            // Atualização do status (ADMIN e OFFICER podem)
            if (reqDto.status() != null && !reqDto.status().equals(orderToBeUpdated.getStatus())) {
                validateStatusTransition(orderToBeUpdated.getStatus(), reqDto.status());
                updatePostedDateIfNeeded(orderToBeUpdated, reqDto.status());

                log.info("Alterando status do pedido {} de '{}' para '{}'",
                        orderId, orderToBeUpdated.getStatus(), reqDto.status());
                orderToBeUpdated.setStatus(reqDto.status());
            }

            orderRepository.persist(orderToBeUpdated);

            log.info("Pedido {} atualizado com sucesso - Status final: {}",
                    orderToBeUpdated.getId(), orderToBeUpdated.getStatus());

            return new UpdateOrderResDto(orderToBeUpdated);

        } catch (Exception e) {
            log.error("Erro ao atualizar pedido ID: {} - Erro: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void updatePostedDateIfNeeded(OrderEntity order, OrderStatus newStatus) {
        boolean isChangingToPosted = OrderStatus.POSTADO.equals(newStatus);
        boolean wasNotPostedBefore = !OrderStatus.POSTADO.equals(order.getStatus());

        if (isChangingToPosted && wasNotPostedBefore) {
            LocalDate currentDate = LocalDate.now();
            order.setPostedDate(currentDate);

            log.info("Pedido {} marcado como POSTADO em {} - Transição de '{}' para '{}'",
                    order.getId(), currentDate, order.getStatus(), newStatus);
        } else if (isChangingToPosted && !wasNotPostedBefore) {
            log.debug("Pedido {} já estava com status POSTADO, mantendo data de postagem: {}",
                    order.getId(), order.getPostedDate());
        }
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        log.debug("Validando transição de status: {} → {}", currentStatus, newStatus);

        if (currentStatus.equals(newStatus)) {
            log.debug("Status não alterado, mantendo: {}", currentStatus);
            return;
        }

        Map<OrderStatus, List<OrderStatus>> allowedTransitions = Map.of(
                OrderStatus.PRODUCAO, List.of(OrderStatus.FINALIZADO),
                OrderStatus.FINALIZADO, List.of(OrderStatus.POSTADO),
                OrderStatus.POSTADO, List.of() // POSTADO é status final
        );

        List<OrderStatus> allowedNextStatuses = allowedTransitions.get(currentStatus);

        if (allowedNextStatuses == null || allowedNextStatuses.isEmpty()) {
            String message = String.format("Status '%s' não permite alterações", currentStatus);
            log.error("Transição inválida para pedido: {}", message);
            throw new InvalidEditOrderStatusParameterException(message);
        }

        if (!allowedNextStatuses.contains(newStatus)) {
            String message = String.format(
                    "Transição inválida: %s → %s. Status permitidos a partir de '%s': %s",
                    currentStatus, newStatus, currentStatus, allowedNextStatuses
            );
            log.error("Transição de status inválida: {}", message);
            throw new InvalidEditOrderStatusParameterException(message);
        }

        log.debug("Transição de status válida: {} → {}", currentStatus, newStatus);
    }

    private void validateDatesForCreation(LocalDate saleDate, LocalDate deliveryDate) {
        log.debug("Validando datas para criação - Venda: {}, Entrega: {}", saleDate, deliveryDate);

        LocalDate today = LocalDate.now();

        if (saleDate.isBefore(today)) {
            log.error("Data de venda no passado: {} (hoje: {})", saleDate, today);
            throw new InvalidDateException("Data de venda não pode ser no passado", saleDate);
        }

        if (deliveryDate.isBefore(today)) {
            log.error("Data de entrega no passado: {} (hoje: {})", deliveryDate, today);
            throw new InvalidDateException("Data de entrega não pode ser no passado", deliveryDate);
        }

        if (deliveryDate.isBefore(saleDate)) {
            log.error("Data de entrega ({}) anterior à data de venda ({})", deliveryDate, saleDate);
            throw new InvalidDateException("Data de entrega deve ser posterior à data de venda", deliveryDate);
        }

        log.debug("Datas válidas para criação");
    }

    private void validateDatesForUpdate(LocalDate saleDate, LocalDate deliveryDate, OrderEntity order) {
        log.debug("Validando datas para atualização do pedido {} - Venda: {}, Entrega: {}",
                order.getId(), saleDate, deliveryDate);

        LocalDate today = LocalDate.now();

        // Para updates, permite datas no passado se o pedido já foi postado
        if (OrderStatus.POSTADO.equals(order.getStatus())) {
            log.debug("Pedido {} já postado, permitindo datas no passado", order.getId());
        } else {
            if (saleDate.isBefore(today)) {
                log.error("Data de venda no passado para pedido não postado: {} (hoje: {})", saleDate, today);
                throw new InvalidDateException("Data de venda não pode ser no passado para pedidos não postados", saleDate);
            }

            if (deliveryDate.isBefore(today)) {
                log.error("Data de entrega no passado para pedido não postado: {} (hoje: {})", deliveryDate, today);
                throw new InvalidDateException("Data de entrega não pode ser no passado para pedidos não postados", deliveryDate);
            }
        }

        if (deliveryDate.isBefore(saleDate)) {
            log.error("Data de entrega ({}) anterior à data de venda ({}) no pedido {}",
                    deliveryDate, saleDate, order.getId());
            throw new InvalidDateException("Data de entrega deve ser posterior à data de venda", deliveryDate);
        }

        log.debug("Datas válidas para atualização do pedido {}", order.getId());
    }

    private int validatePage(Integer page) {
        if (page == null || page < 0) {
            log.debug("Página inválida ({}), usando padrão: 0", page);
            return 0;
        }
        return page;
    }

    private int validatePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            log.debug("Tamanho da página inválido ({}), usando padrão: {}", pageSize, DEFAULT_PAGE_SIZE);
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            log.debug("Tamanho da página muito grande ({}), limitando a: {}", pageSize, MAX_PAGE_SIZE);
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    public Map<OrderStatus, Long> getOrderStatistics() {

        log.debug("Getting order stastistics");

        try {
            Map<OrderStatus, Long> stats = Map.of(
                    OrderStatus.PRODUCAO, orderRepository.count("status", OrderStatus.PRODUCAO),
                    OrderStatus.FINALIZADO, orderRepository.count("status", OrderStatus.FINALIZADO),
                    OrderStatus.POSTADO, orderRepository.count("status", OrderStatus.POSTADO)
            );

            log.info("Estatísticas de pedidos: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("Erro ao gerar estatísticas de pedidos: {}", e.getMessage(), e);
            throw new OrderServiceException("Stastistics generation failed!", e);
        }
    }

}