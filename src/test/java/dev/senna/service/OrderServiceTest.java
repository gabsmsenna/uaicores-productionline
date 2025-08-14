package dev.senna.service;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.exception.ClientAlreadyExistsException;
import dev.senna.exception.ClientNotFoundException;
import dev.senna.exception.InvalidDateException;
import dev.senna.exception.OrderNotFoundException;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.Material;
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
            var saleDate = LocalDate.now();
            var deliveryDate = saleDate.plusDays(5);
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
            assertNotNull(orderPersisted);

            verify(clientRepository, times(1)).findByIdOptional(client.getClientId());
            verify(orderRepository, times(1)).persist(orderEntityCaptor.capture());

            var orderCaptured = orderEntityCaptor.getValue();

            assertEquals(saleDate, orderCaptured.getSaleDate());
            assertEquals(deliveryDate, orderCaptured.getDeliveryDate());
            assertEquals(client, orderCaptured.getClient());
            assertEquals(expectedOrderId, orderPersisted);
            assertEquals(OrderStatus.PRODUCAO, orderCaptured.getStatus());
        }

        @Test
        @DisplayName("Should return client not found exception when client id does not exists on database")
        void shouldReturnClientNotFoundExceptionWhenClientIdDoesNotExistsOnDatabase() {

            // Arrange
            var saleDate = LocalDate.now();
            var deliveryDate = saleDate.plusDays(5);
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
            var result = orderService.listOrders(null, null, page, pageSize);

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
            var result = orderService.listOrders(null, null,0, 10);

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
                orderService.listOrders(null, null, page, pageSize);
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
            var resultList = orderService.listOrders(null, null,0, 10);

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
            var resultList = orderService.listProduction(page, pageSize);

            // Assert
            assertTrue(resultList.isEmpty());
            verify(orderRepository, times(1)).listOrdersInProduction(page, pageSize);
        }
    }

    @Nested
    @DisplayName("listLastSendOrders() tests")
    class listLastSendOrders {

        private OrderEntity orderEntity1;
        private OrderEntity orderEntity2;

        @BeforeEach
        void setUp() {
            // Setup client
            ClientEntity clientEntity = new ClientEntity();
            clientEntity.setClientName("CLIENT_NAME");

            // Setup items
            ItemEntity itemEntity1 = new ItemEntity();
            itemEntity1.setName("ITEM_1");
            itemEntity1.setQuantity(1000);
            itemEntity1.setSaleQuantity(1000);
            itemEntity1.setMaterial(Material.ELETROSTATICO);
            itemEntity1.setImage("IMG_URL");
            itemEntity1.setStatus(ItemStatus.EMBALADO);

            ItemEntity itemEntity2 = new ItemEntity();
            itemEntity2.setName("ITEM_2");
            itemEntity2.setQuantity(5000);
            itemEntity2.setSaleQuantity(5000);
            itemEntity2.setMaterial(Material.ADESIVO);
            itemEntity2.setImage("IMG_URL_2");
            itemEntity2.setStatus(ItemStatus.EMBALADO);

            ItemEntity itemEntity3 = new ItemEntity();
            itemEntity3.setName("ITEM_3");
            itemEntity3.setQuantity(1000);
            itemEntity3.setSaleQuantity(1000);
            itemEntity3.setMaterial(Material.LONA);
            itemEntity3.setImage("IMG_URL_3");
            itemEntity3.setStatus(ItemStatus.EMBALADO);

            ItemEntity itemEntity4 = new ItemEntity();
            itemEntity4.setName("ITEM_4");
            itemEntity4.setQuantity(2000);
            itemEntity4.setSaleQuantity(2000);
            itemEntity4.setMaterial(Material.BRANCO_FOSCO);
            itemEntity4.setImage("IMG_URL_4");
            itemEntity4.setStatus(ItemStatus.EMBALADO);


            // Setup orders
            orderEntity1 = new OrderEntity();
            orderEntity1.setId(1L);
            orderEntity1.setClient(clientEntity);
            orderEntity1.setItems(List.of(itemEntity1, itemEntity2));
            orderEntity1.setDeliveryDate(LocalDate.of(2024, 1, 15));
            orderEntity1.setPostedDate(LocalDate.of(2024, 1, 19));

            orderEntity2 = new OrderEntity();
            orderEntity2.setId(2L);
            orderEntity2.setClient(clientEntity);
            orderEntity2.setItems(List.of(itemEntity1, itemEntity2));
            orderEntity2.setDeliveryDate(LocalDate.of(2024, 1, 15));
            orderEntity2.setPostedDate(LocalDate.of(2024, 1, 19));

            // Link items to order
            itemEntity1.setOrder(orderEntity1);
            itemEntity2.setOrder(orderEntity1);
            itemEntity3.setOrder(orderEntity2);
            itemEntity4.setOrder(orderEntity2);
        }

        @Test
        @DisplayName("Should return a paginated list of last send orders")
        void shouldReturnAPaginatedListOfLastSendOrders() {

            // Arrange
            var page = 0;
            var pageSize = 10;
            var orderEntities = List.of(orderEntity1, orderEntity2);

            when(orderRepository.findLastSent(page, pageSize)).thenReturn(mockPanache);
            when(mockPanache.list()).thenReturn(orderEntities);

            // Act
            var result = orderService.listLastSendOrders(page, pageSize);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            var orderRespDto = result.getFirst();
            assertEquals("CLIENT_NAME", orderRespDto.clientName());
            assertEquals(LocalDate.of(2024, 1, 19), orderRespDto.sendDate());

            var itemsRespDto = orderRespDto.items();
            assertEquals(2, itemsRespDto.size());

            verify(orderRepository).findLastSent(page, pageSize);
            verify(mockPanache).list();
        }
    }

    @Nested
    @DisplayName("updateOrder() tests")
    class updateOrder {

        private OrderEntity actualOrder;
        private ClientEntity clientEntity;
        private UUID clientId;
        private UpdateOrderReqDto reqDto;

        @BeforeEach
        void setUp() {

            clientEntity = new ClientEntity();
            clientId = UUID.randomUUID();
            clientEntity.setClientId(clientId);
            clientEntity.setClientName("CLIENT_NAME");

            actualOrder = new OrderEntity();
            actualOrder.setId(1L);
            actualOrder.setSaleDate(LocalDate.now());
            actualOrder.setPostedDate(actualOrder.getSaleDate().plusDays(5));
            actualOrder.setStatus(OrderStatus.PRODUCAO);
            actualOrder.setClient(clientEntity);

            reqDto = new UpdateOrderReqDto(OrderStatus.FINALIZADO, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), clientEntity.getClientId());
        }

        @Test
        @DisplayName("Should update an order successfully with valid data when not changing client")
        void shouldUpdateAnOrderSuccessfully() {

            // Arrange
            Long orderId = 1L;
            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(actualOrder));

            // Act
            orderService.updateOrder(orderId, reqDto);

            // Assert
            verify(orderRepository).findByIdOptional(orderId);
            verify(orderRepository).persist(orderEntityCaptor.capture());
            var capturedOrder = orderEntityCaptor.getValue();

            assertNotNull(capturedOrder);
            assertEquals(capturedOrder.getStatus(), reqDto.status());
            assertEquals(reqDto.deliveryDate(), capturedOrder.getDeliveryDate());
            assertEquals(clientEntity, capturedOrder.getClient()); // Verifica se o cliente foi trocado
            assertEquals(orderId, capturedOrder.getId()); // O ID não deve mudar


        }

        @Test
        @DisplayName("Should update an order successfully when changing to a new valid client")
        void shouldUpdateAnOrderSuccessfullyWhenChangingToANewValidClient() {

            // Arrange
            Long orderId = 1L;

            var newClient = new ClientEntity(UUID.randomUUID(), "NEW_CLIENT_NAME");
            var reqDtoWithNewClient  = new UpdateOrderReqDto(OrderStatus.FINALIZADO, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), newClient.getClientId());

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(actualOrder));
            when(clientRepository.findByIdOptional(newClient.getClientId())).thenReturn(Optional.of(newClient));

            // Act
            orderService.updateOrder(orderId, reqDtoWithNewClient);

            // Assert
            verify(clientRepository).findByIdOptional(newClient.getClientId());
            verify(orderRepository).persist(orderEntityCaptor.capture());
            var capturedOrder = orderEntityCaptor.getValue();

            assertNotNull(capturedOrder);
            assertEquals(newClient, capturedOrder.getClient());
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order not found")
        void shouldThrowOrderNotFoundException() {

            // Arrange
            Long orderId = 2L;

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.empty());

            // Act
            var exception = assertThrows(OrderNotFoundException.class, () -> {
                orderService.updateOrder(orderId, reqDto);
            });

            // Assert
            assertEquals("Order with id " + orderId + " not found on the application", exception.getDetail());
            verify(orderRepository, times(1)).findByIdOptional(orderId);
            verify(orderRepository, never()).persist(any(OrderEntity.class));
            verify(clientRepository, never()).findByIdOptional(clientId);
        }

        @Test
        @DisplayName("Should throw ClientNotFoundException when client not found")
        void shouldThrowClientNotFoundException() {

            // Arrange
            Long orderId = 3L;
            var nonExistentClientId = UUID.randomUUID();
            reqDto = new UpdateOrderReqDto(OrderStatus.FINALIZADO, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), nonExistentClientId);
            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(actualOrder));
            when(clientRepository.findByIdOptional(nonExistentClientId)).thenReturn(Optional.empty());

            // Act
            var exception = assertThrows(ClientNotFoundException.class, () -> {
                orderService.updateOrder(orderId, reqDto);
            });

            // Assert
            assertEquals("Client with id " + nonExistentClientId + " not found", exception.getDetail());
            verify(orderRepository).findByIdOptional(orderId);
            verify(clientRepository).findByIdOptional(nonExistentClientId);
            verify(orderRepository, never()).persist(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should throw InvalidDateException when sale date is in the past")
        void shouldThrowInvalidDateExceptionWhenSaleDateIsInThePast() {

            Long orderId = 1L;
            var invalidReqDto = new UpdateOrderReqDto(
                    OrderStatus.PRODUCAO,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5),
                    clientId
            );

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(actualOrder));

            var exception = assertThrows(InvalidDateException.class, () -> {
                orderService.updateOrder(orderId, invalidReqDto);
            });

            assertEquals("The specified date is not valid for this operation. " + invalidReqDto.saleDate(), exception.getDetail());
            verify(orderRepository, never()).persist(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should throw InvalidDateException when delivery date is in the past")
        void shouldThrowInvalidDateExceptionWhenDeliveryDateIsInThePast() {

            Long orderId = 1L;

            var invalidReqDto = new UpdateOrderReqDto(
                    OrderStatus.PRODUCAO,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().minusDays(1),
                    clientId
            );

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(actualOrder));

            var exception = assertThrows(InvalidDateException.class, () -> {
                orderService.updateOrder(orderId, invalidReqDto);
            });

            assertEquals("The specified date is not valid for this operation. " + invalidReqDto.deliveryDate(), exception.getDetail());
            verify(orderRepository, never()).persist(any(OrderEntity.class));
        }


    }
}