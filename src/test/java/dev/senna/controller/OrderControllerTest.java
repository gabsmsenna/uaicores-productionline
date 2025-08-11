package dev.senna.controller;

import dev.senna.controller.dto.request.CreateOrderReqDto;
import dev.senna.controller.dto.request.UpdateOrderReqDto;
import dev.senna.controller.dto.response.UpdateOrderResDto;
import dev.senna.model.enums.OrderStatus;
import dev.senna.service.OrderService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @InjectMock
    OrderService orderService;

    private static final String BASE_PATH = "/order";

    @Nested
    @DisplayName("POST /order - createOrder")
    class CreateOrderTests {


        @Test
        @DisplayName("Should return a 201 Created when order created successfully")
        void shouldReturnCreatedWhenOrderIsValid() {

            var validDto = new CreateOrderReqDto(
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    UUID.randomUUID()
            );

            var expectedOrderId = 1L;

            when(orderService.createOrder(any(CreateOrderReqDto.class))).thenReturn(expectedOrderId);

            given()
                    .contentType(ContentType.JSON)
                    .body(validDto)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(201)
                    .header("Location", notNullValue())
                    .header("Location",endsWith("/order/" + expectedOrderId));
        }

        @Test
        @DisplayName("Should return 400 when DTO is invalid (null dates)")
        void shouldReturn400WhenDTOIsInvalid() {

            var invalidDto = new CreateOrderReqDto(null, null, UUID.randomUUID());

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidDto)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Should return 500 when an Internal Server Error is thrown")
        void shouldReturn500WhenAnInternalServerError() {

            var validDto = new CreateOrderReqDto(
                    LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    UUID.randomUUID()
            );

            Mockito.when(orderService.createOrder(any(CreateOrderReqDto.class)))
                    .thenThrow(new RuntimeException("Unespected error on database"));

            given()
                    .contentType(ContentType.JSON)
                    .body(validDto)
                    .when()
                    .post(BASE_PATH)
                    .then()
                    .statusCode(500);
        }
    }

    @Nested
    @DisplayName("GET /order - listOrders")
    class ListOrderTests {

        @Test
        @DisplayName("Should return 200 withi an order list")
        void shouldReturn200WithiAnOrderList() {

            when(orderService.listOrders(0, 10)).thenReturn(Collections.emptyList());

            given()
                    .queryParam("page", 0)
                    .queryParam("pageSize", 10)
                    .accept(ContentType.JSON)
                    .when()
                    .get(BASE_PATH)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", is(0));
        }

        @Test
        @DisplayName("Should return 500 when internal server error is thrown")
        void shouldReturn500WhenInternalServerError() {

            when(orderService.listOrders(0, 10))
                    .thenThrow(new RuntimeException("Fail when trying to list orders"));

            given()
                    .queryParam("page", 0)
                    .queryParam("pageSize", 10)
                    .accept(ContentType.JSON)
                    .when()
                    .get(BASE_PATH)
                    .then()
                    .statusCode(500);
            }
        }

        @Nested
        @DisplayName("PUT /order/{orderId} - updateOrder")
        class UpdateOrderTests {
            @Test
            @DisplayName("Deve retornar 200 OK e o pedido atualizado quando a atualização é bem-sucedida")
            void shouldReturnOkWhenUpdateIsSuccessful() {
                Long orderId = 1L;
                UUID clientId = UUID.randomUUID();

                UpdateOrderReqDto updateReqDto = new UpdateOrderReqDto(
                        OrderStatus.POSTADO,
                        LocalDate.now(),
                        LocalDate.now().plusDays(1),
                        clientId
                );

                UpdateOrderResDto responseDto = new UpdateOrderResDto(
                        Collections.emptyList(), // Exemplo com lista de itens vazia
                        OrderStatus.POSTADO,
                        clientId
                );

                Mockito.when(orderService.updateOrder(eq(orderId), any(UpdateOrderReqDto.class)))
                        .thenReturn(responseDto);

                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .pathParam("orderId", orderId)
                        .body(updateReqDto)
                        .when()
                        .put(BASE_PATH + "/{orderId}")
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.JSON)
                        .body("status", is(OrderStatus.POSTADO.toString()))
                        .body("clientId", is(clientId.toString()));
            }

            @Test
            @DisplayName("Deve retornar 400 Bad Request se o corpo da requisição for inválido (sem @Valid, mas pode ser testado)")
            void shouldReturnBadRequestForInvalidBody() {
                // Mesmo que não haja @Valid, um corpo malformado ou com tipos errados causará um erro 400
                String malformedJson = "{\"status\":\"INVALID_STATUS_VALUE\"}";
                Long orderId = 1L;

                given()
                        .contentType(ContentType.JSON)
                        .pathParam("orderId", orderId)
                        .body(malformedJson)
                        .when()
                        .put(BASE_PATH + "/{orderId}")
                        .then()
                        // A deserialização para o Enum OrderStatus falhará, resultando em um erro de requisição.
                        // O código exato pode variar (400 ou 422), mas será da família 4xx. 400 é o mais comum.
                        .statusCode(400);
            }

            @Test
            @DisplayName("Deve retornar 500 Internal Server Error quando o serviço lança uma exceção")
            void shouldReturnInternalServerErrorWhenServiceThrowsException() {
                Long orderId = 99L;
                UpdateOrderReqDto updateDto = new UpdateOrderReqDto(OrderStatus.POSTADO, null, null, null);

                Mockito.when(orderService.updateOrder(eq(orderId), any(UpdateOrderReqDto.class)))
                        .thenThrow(new RuntimeException("Não foi possível encontrar o pedido para atualizar"));

                given()
                        .contentType(ContentType.JSON)
                        .pathParam("orderId", orderId)
                        .body(updateDto)
                        .when()
                        .put(BASE_PATH + "/{orderId}")
                        .then()
                        .statusCode(500);
            }
        }
    }
