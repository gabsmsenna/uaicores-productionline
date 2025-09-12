package dev.senna.controller;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.exception.OrderServiceException;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.Material;
import dev.senna.model.enums.OrderStatus;
import dev.senna.repository.ClientRepository;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import dev.senna.service.OrderService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;


@QuarkusTest
@DisplayName("OrderController Integration Test")
class OrderControllerIT {

    @Inject
    OrderRepository orderRepository;

    @Inject
    ClientRepository clientRepository;

    @Inject
    ItemRepository itemRepository;

    @Inject
    OrderService orderService;

    private ClientEntity testClient;

    private OrderEntity testOrder;

    private ItemEntity testItem;

    @BeforeEach
    @Transactional
    void setUp() {

        // Limpando dados de teste
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        itemRepository.deleteAll();

        // Criando cliente de teste
        testClient = new ClientEntity();
        testClient.setClientName("testClient");
        clientRepository.persist(testClient);

        // Criando pedido de teste
        testOrder = new OrderEntity();
        testOrder.setSaleDate(LocalDate.now());
        testOrder.setDeliveryDate(LocalDate.now().plusDays(5));
        testOrder.setClient(testClient);
        testOrder.setStatus(OrderStatus.PRODUCAO);
        orderRepository.persist(testOrder);

        // Criando item de teste
        testItem = new ItemEntity();
        testItem.setId(1L);
        testItem.setName("ITEM_NAME");
        testItem.setQuantity(900);
        testItem.setMaterial(Material.ADESIVO);
        testItem.setSaleQuantity(1000);
        testItem.setImage("URL_IMAGE");
        testItem.setOrder(testOrder);
        itemRepository.persist(testItem);
    }

    @Nested
    @DisplayName("POST /order tests")
    class postOrderTests {

        @Test
        @DisplayName("Should create an order successfully")
        void shouldCreateAnOrder() {

            var request = new CreateOrderReqDto(
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    testClient.getClientId()
            );

            given()
            .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/order")
                    .then()
                    .statusCode(201)
                    .header("Location", notNullValue())
                    .header("Location", containsString("/order"));
        }

        @Test
        @DisplayName("Should return 400 when trying to create an order with invalid data")
        void shouldReturn400WhenTryingToCreateAnOrder() {

            var request = new CreateOrderReqDto(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5),
                    testClient.getClientId()
            );

            given()
            .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/order")
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should return 404 when trying to create an order with inexistent client")
        void shouldReturn404WhenTryingToCreateAnOrderWithInvalidClient() {

            var request = new CreateOrderReqDto(
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    UUID.randomUUID()
            );

            given()
            .contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/order")
                    .then()
                    .statusCode(404);

        }
    }

    @Nested
    @DisplayName("GET /order tests")
    class getOrderTests {

        @Test
        @DisplayName("Should list orders with pagination")
        void shouldListOrders() {

            given()
                    .when()
                    .get("/order")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(greaterThanOrEqualTo(1)))
                    .body("[0].clientName", equalTo(testClient.getClientName()))
                    .body("[0].status", equalTo(OrderStatus.PRODUCAO));
        }

        @Test
        @DisplayName("Should return paginated orders list with filter")
        void shouldReturnPaginatedOrders() {

            given()
                    .queryParam("page", 0)
                    .queryParam("size", 10)
                    .when()
                    .get("/order")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(greaterThanOrEqualTo(1)));
        }
    }

    @Nested
    @DisplayName("GET /order/production tests")
    class getProductionTests {

        @Test
        @DisplayName("Should list orders in production")
        void shouldListOrdersInProduction() {

            given()
                    .when()
                    .get("/order/production")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(greaterThanOrEqualTo(1)))
                    .body("[0].status", equalTo(OrderStatus.PRODUCAO))
                    .body("[0].clientName", equalTo(testClient.getClientName()))
                    .body("[0].items", hasSize(1))
                    .body("[0].items[0].name", equalTo(testOrder.getItems().get(0).getName()));
        }

        @Test
        @DisplayName("Should return an empty list when no orders are in production")
        @Transactional
        void shouldReturnAnEmptyListWhenNoOrdersAreInProduction() {

            itemRepository.deleteAll();
            orderRepository.deleteAll();

            given()
                    .when()
                    .get("/order/production")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }

        @Test
        @DisplayName("Should respect pagination parameters")
        @Transactional
        void shouldRespectPagination() {

            var client = clientRepository.find("clientName",  testClient.getClientName()).firstResult();

            var orderInProduction = new OrderEntity();
            orderInProduction.setClient(client);
            orderInProduction.setStatus(OrderStatus.PRODUCAO);
            orderInProduction.setSaleDate(LocalDate.now());
            orderInProduction.setDeliveryDate(LocalDate.now().plusDays(5));
            orderInProduction.setId(2L);
            orderRepository.persist(orderInProduction);

            given()
                    .queryParam("page", 0)
                    .queryParam("pageSize", 1)
                    .when()
                    .get("/order/production")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1));

        }
    }

    @Nested
    @DisplayName("GET /last-send-orders tests")
    class getLastSendOrdersTests {

        @Test
        @DisplayName("Should list last send orders with default value")
        void shouldListLastSendOrdersWhenGiveNoParameters() {

            given()
                    .when()
                    .get("/last-send-orders")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(1))
                    .body("[0].clientName", equalTo(testClient.getClientName()))
                    .body("[0].items", hasSize(1))
                    .body("[0].items[0].name", equalTo(testOrder.getItems().get(0).getName()));
        }

        @Test
        @DisplayName("Should return first page when give an invalid page number")
        void shouldReturnFirstPageWhenGiveInvalidPageNumber() {

            given()
                    .queryParam("page", -1)
                    .when()
                    .get("/last-send-orders")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(1))
                    .body("[0].clientName", equalTo(testClient.getClientName()));
        }
    }

    @Nested
    @DisplayName("PATCH /order/orderId tests")
    class patchOrderIdTests {

        @Test
        @DisplayName("Should update all fields and return 200 ok")
        void shouldUpdateAllFieldsAndReturn200Ok() {

            var newSaleDate = LocalDate.now().plusDays(1);
            var newDeliveryDate = LocalDate.now().plusDays(6);
            var newStatus = OrderStatus.FINALIZADO;

            var reqDto = new UpdateOrderReqDto(newStatus, newSaleDate, newDeliveryDate, null);

            given()
                    .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(200)
                    .body("status", is(newStatus.toString()))
                    .body("saleDate", is(newSaleDate.toString()))
                    .body("deliveryDate", is(newDeliveryDate.toString()))
                    .body("items", notNullValue());
        }

        @Test
        @DisplayName("Should update only one field and return 200 OK")
        void shouldUpdateOnlyOneFieldAndReturn200Ok() {

            var newStatus = OrderStatus.FINALIZADO;
            var reqDto = new UpdateOrderReqDto(newStatus, null, null, null);

            given()
                    .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(200)
                    .body("status", is(newStatus.toString()))
                    .body("clientId", is(testClient.getClientId().toString()));
        }

        @Test
        @DisplayName("Should return 404 when trying update an inexistent order")
        void shouldReturn404WhenTryingUpdateAnInexistentOrder() {

            var nonExistentOrderId = 123L;

            var reqDto = new UpdateOrderReqDto(OrderStatus.POSTADO, null, null, null);

            given()
                    .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + nonExistentOrderId)
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when trying update client with an inexistent clientId")
        void shouldReturn404WhenTryingUpdateClientWithAnInexistentClientId() {

            var nonExistentClientId = UUID.randomUUID();
            var reqDto = new UpdateOrderReqDto(null, null, null, nonExistentClientId);

            given()
                .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Should return 400 to a status transition invalid")
        void shouldReturn400ToAStatusTransitionInvalid() {

            testOrder.setStatus(OrderStatus.FINALIZADO);
            orderRepository.persist(testOrder);

            var reqDto = new UpdateOrderReqDto(OrderStatus.PRODUCAO, null, null, null);

            given()
                    .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 if delivery date is previous sale date")
        void shouldReturn400IfDeliveryDateIsBehindSaleDate() {

            var saleDate = LocalDate.now().plusDays(5);
            var invalidDeliveryDate = LocalDate.now().plusDays(4);

            var reqDto = new UpdateOrderReqDto(null, saleDate, invalidDeliveryDate, null);

            given()
                    .contentType(ContentType.JSON)
                    .body(reqDto)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should return 400 if request body is null")
        void shouldReturn400IfRequestBodyIsNull() {

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .patch("/order/" + testOrder.getId())
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /order/statistics tests")
    class getOrderStatistics {

        @Test
        @DisplayName("Should list order statistics and return correct counts")
        void shouldListOrderStatisticsAndCorrectCounts() {

            orderRepository.persist(new OrderEntity(OrderStatus.PRODUCAO));
            orderRepository.persist(new OrderEntity(OrderStatus.FINALIZADO));
            orderRepository.persist(new OrderEntity(OrderStatus.FINALIZADO));
            orderRepository.persist(new OrderEntity(OrderStatus.PRODUCAO));
            orderRepository.persist(new OrderEntity(OrderStatus.POSTADO));

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/order/statistics")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("PRODUCAO", is(2))
                    .body("FINALIZADO", is(2))
                    .body("POSTADO", is(1));
        }

        @Test
        @DisplayName("Should return zero counts when no order exists")
        void shouldReturnZeroCountsWhenNoOrderExists() {

            given()
                    .when()
                    .get("/order/statistics")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("PRODUCAO", is(0))
                    .body("FINALIZADO", is(0))
                    .body("POSTADO", is(0));
        }

        @Test
        @DisplayName("Should return 500 when service fails")
        void shouldReturn500WhenServiceFails() {

            when(orderService.getOrderStatistics()).
                    thenThrow(new OrderServiceException("Database connection failed", new RuntimeException()));

            given()
                    .when()
                    .get("/order/statistics")
                    .then()
                    .statusCode(500);
        }
    }
}