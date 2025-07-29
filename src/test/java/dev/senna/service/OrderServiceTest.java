package dev.senna.service;

import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.exception.ClientNotFoundException;
import dev.senna.exception.OrderNotFoundException;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private ItemRepository itemRepositoryMock;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    @Captor
    private ArgumentCaptor<OrderEntity> orderEntityCaptor;

    @Mock
    PanacheQuery<OrderEntity> mockPanache;

    @Nested
    @DisplayName("createOrder() tests")
    class createOrder {

        @Test
        @DisplayName("Should create an order successfully")
        void shouldCreateAnOrderSuccessfully() {

            // Arrange
            var saleDate = LocalDate.of(2025, 7, 28);
            var deliveryDate = LocalDate.of(2025, 8, 3);
            var client = new ClientEntity(UUID.randomUUID(), "CLIENT_NAME");
            var createOrderReqDto = new CreateOrderReqDto(saleDate, deliveryDate, client.getClientId());
            when(clientRepository.findByIdOptional(client.getClientId())).thenReturn(Optional.of(client));
            var expectedOrderId = 123L;

            doAnswer(invocationOnMock -> {
                OrderEntity persistedOrder = invocationOnMock.getArgument(0);
                persistedOrder.setId(expectedOrderId);
                return null;
            }).when(orderRepository).persist(any(OrderEntity.class));

            // Act
            var orderPersisted = orderService.createOrder(createOrderReqDto);

            // Assert
            assertNotNull(orderPersisted.getId());

            verify(clientRepository, times(1)).findByIdOptional(client.getClientId());
            verify(orderRepository, times(1)).persist(orderEntityCaptor.capture());

            var orderCaptured = orderEntityCaptor.getValue();

            assertEquals(saleDate, orderCaptured.getSaleDate());
            assertEquals(deliveryDate, orderCaptured.getDeliveryDate());
            assertEquals(client, orderCaptured.getClient());
            assertEquals(expectedOrderId, orderPersisted.getId());
            assertEquals(OrderStatus.PRODUCAO, orderPersisted.getStatus());
        }

        @Test
        @DisplayName("Should return client not found exception when client id does not exists on database")
        void shouldReturnClientNotFoundExceptionWhenClientIdDoesNotExistsOnDatabase() {

            // Arrange
            var saleDate = LocalDate.of(2025, 7, 28);
            var deliveryDate = LocalDate.of(2025, 8, 3);
            var client = new ClientEntity(UUID.randomUUID(), "CLIENT_NAME");
            var createOrderReqDto = new CreateOrderReqDto(saleDate, deliveryDate, client.getClientId());
            when(clientRepository.findByIdOptional(client.getClientId())).thenReturn(Optional.empty());

            // Act and Asert
            var exception = assertThrows(ClientNotFoundException.class, () -> {
                orderService.createOrder(createOrderReqDto);
            });

            // Assert
            verify(clientRepository, times(1)).findByIdOptional(client.getClientId());
            verify(orderRepository, never()).persist(any(OrderEntity.class));
            assertEquals("Client with id " + client.getClientId() + " not found", exception.getDetail());
        }

        @Test
        @DisplayName("Should throw an expcetion when trying to persist an order and fails")
        void shouldThrowExceptionWhenPersistenceFails() {

            // Arrange
            var client = new ClientEntity(UUID.randomUUID(), "CLIENT_NAME");
            var createOrderReqDto = new CreateOrderReqDto(LocalDate.now(), LocalDate.now().plusDays(5), client.getClientId());
            when(clientRepository.findByIdOptional(client.getClientId())).thenReturn(Optional.of(client));
            doThrow(new PersistenceException("Error while trying to persist order"))
                    .when(orderRepository)
                    .persist(any(OrderEntity.class));

            // Act and Assert
            assertThrows(PersistenceException.class, () -> {
                orderService.createOrder(createOrderReqDto);
            });

            verify(clientRepository, times(1)).findByIdOptional(client.getClientId());
            verify(orderRepository, times(1)).persist(any(OrderEntity.class));
        }

    }

    @Nested
    @DisplayName("listOrders() tests")
    class listOrders {

        @Test
        @DisplayName("Should return a paginated list of orders dto")
        void shouldReturnAPaginatedListOfOrdersDto() {

            // Assert
            Integer page = 0;
            Integer pageSize = 10;

            var client1 = new ClientEntity(UUID.randomUUID(), "CLIENTE_A");
            var order1 = new OrderEntity();
            order1.setClient(client1);
            order1.setSaleDate(LocalDate.now());
            order1.setDeliveryDate(LocalDate.now().plusDays(5));
            order1.setStatus(OrderStatus.PRODUCAO);

            var client2 = new ClientEntity(UUID.randomUUID(), "CLIENT_B");
            var order2 = new OrderEntity();
            order2.setClient(client2);
            order2.setSaleDate(LocalDate.now());
            order2.setDeliveryDate(LocalDate.now().plusDays(7));
            order2.setStatus(OrderStatus.POSTADO);

            List<OrderEntity> mockOrderList = List.of(order1, order2);

            var mockQuery = mock(PanacheQuery.class);
            when(orderRepository.findAll()).thenReturn(mockQuery);
            when(mockQuery.page(any(Page.class))).thenReturn(mockQuery);
            when(mockQuery.list()).thenReturn(mockOrderList);

            // Act
            var result = orderService.listOrders(page, pageSize);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            var dto1 = result.getFirst();
            assertEquals(client1.getClientName(), dto1.clientName());
            assertEquals(order1.getSaleDate(), dto1.saleDate());
            assertEquals(order1.getDeliveryDate(), dto1.deliveryDate());
            assertEquals(order1.getStatus(), dto1.status());

            var dto2 = result.get(1);
            assertEquals(client2.getClientName(), dto2.clientName());
            assertEquals(order2.getSaleDate(), dto2.saleDate());
            assertEquals(order2.getDeliveryDate(), dto2.deliveryDate());
            assertEquals(order2.getStatus(), dto2.status());

            verify(orderRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return an empty list when no orders were found")
        void shouldReturnAnEmptyListWhenNoOrdersWereFound() {

            // Arrange
            Integer page = 0;
            Integer pageSize = 10;

            when(orderRepository.findAll()).thenReturn(mockPanache);
            when(mockPanache.page(any(Page.class))).thenReturn(mockPanache);
            when(mockPanache.list()).thenReturn(List.of());

            // Act
            var result = orderService.listOrders(0, 10);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should return null pointer exception when clientId is null")
        void shouldReturnClientNotFoundExceptionWhenClientIdIsNull() {
            // Arrange
            Integer page = 0;
            Integer pageSize = 10;

            var order = new OrderEntity();
            order.setClient(null);
            order.setSaleDate(LocalDate.now());
            order.setDeliveryDate(LocalDate.now().plusDays(5));
            order.setStatus(OrderStatus.PRODUCAO);

            when(orderRepository.findAll()).thenReturn(mockPanache);
            when(mockPanache.page(any(Page.class))).thenReturn(mockPanache);
            when(mockPanache.list()).thenReturn(List.of(order));

            // Act and Assert
            assertThrows(NullPointerException.class, () -> {
                orderService.listOrders(page, pageSize);
            });

        }
    }

    @Nested
    @DisplayName("listOrdersInProduction() tests")
    class listOrdersInProduction {

        @Test
        @DisplayName("Should return a paginated list of orders in production")
        void shouldReturnAPaginatedListOfOrdersInProduction() {

            // Arrange
            var page = 0;
            var pageSize = 10;

            var client1 = new ClientEntity(UUID.randomUUID(), "CLIENTE_A");
            var order1 = new OrderEntity();
            order1.setClient(client1);
            order1.setSaleDate(LocalDate.now());
            order1.setDeliveryDate(LocalDate.now().plusDays(5));
            order1.setStatus(OrderStatus.PRODUCAO);

            var client2 = new ClientEntity(UUID.randomUUID(), "CLIENT_B");
            var order2 = new OrderEntity();
            order2.setClient(client2);
            order2.setSaleDate(LocalDate.now());
            order2.setDeliveryDate(LocalDate.now().plusDays(7));
            order2.setStatus(OrderStatus.PRODUCAO);

            List<OrderEntity> mockOrderList = List.of(order1, order2);

            given(orderRepository.listOrdersInProduction(page, pageSize)).willReturn(mockOrderList);

            // Act
            var resultList = orderService.listOrdersInProduction(0, 10);

            // Assert
            var dto1 = resultList.getFirst();
            var dto2 = resultList.get(1);

            assertAll(
                    () -> assertEquals(2, resultList.size()),
                    () -> assertEquals(order1.getSaleDate(), dto1.saleDate()),
                    () -> assertEquals(order1.getDeliveryDate(), dto1.deliveryDate()),
                    () -> assertEquals(order1.getClient().getClientName(), dto1.clientName()),
                    () -> assertEquals(OrderStatus.PRODUCAO, dto1.status()),

                    () -> assertEquals(order2.getSaleDate(), dto2.saleDate()),
                    () -> assertEquals(order2.getDeliveryDate(), dto2.deliveryDate()),
                    () -> assertEquals(order2.getClient().getClientName(), dto2.clientName()),
                    () -> assertEquals(OrderStatus.PRODUCAO, dto1.status())
            );

            verify(orderRepository, times(1)).listOrdersInProduction(page, pageSize);
        }

        @Test
        @DisplayName("Should return an empty list when no orders in production")
        void shouldReturnAnEmptyListWhenNoOrdersInProduction() {

            // Arrange
            var page = 0;
            var pageSize = 10;

            given(orderRepository.listOrdersInProduction(page, pageSize)).willReturn(Collections.emptyList());

            // Act
            var resultList = orderService.listOrdersInProduction(page, pageSize);

            // Assert
            assertTrue(resultList.isEmpty());
            verify(orderRepository, times(1)).listOrdersInProduction(page, pageSize);
        }



    }

}