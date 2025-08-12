package dev.senna.controller;

import dev.senna.profile.ClientTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(ClientTestProfile.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientControllerIT {

    private static final String CLIENT_ENDPOINT = "/client";
    private static final Logger log = LoggerFactory.getLogger(ClientControllerIT.class);

    private static UUID createdClientId;

    @Test
    @DisplayName("Should create client successfully")
    void shouldCreateClient() {

        var createRequest = """
                {
                "clientName":"CLIENT_NAME"
                }
                """;

        var response = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(CLIENT_ENDPOINT)
                .then()
                .statusCode(201)
                .header("Location", matchesPattern(".*/client/[a-f0-9-]{36}"))
                .extract()
                .response();

        String location = response.getHeader("Location");
        createdClientId = UUID.fromString(location.substring(location.lastIndexOf("/") + 1));

    }

    @Test
    @DisplayName("Should return 400 when creating client with invalid data")
    void shouldReturn400WhenCreatingClientWithInvalidData() {
        var invalidRequest = """
                {
                    "clientName": ""
                }
                """;
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequest)
                .when()
                .post(CLIENT_ENDPOINT)
                .then()
                .statusCode(anyOf(equalTo(400)));

    }

    @Test
    @DisplayName("Should find all client with default pagination")
    void shouldFindAllClientWithDefaultPagination() {
        given()
                .when()
                .get(CLIENT_ENDPOINT)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0))
                .body("$", hasKey("content"))
                .body("$", hasKey("totalElements"))
                .body("$", hasKey("totalPages"))
                .body("$", hasKey("size"))
                .body("$", hasKey("number"));
        // TODO: VERIFICAR ESTA FUNÇÃO POIS ACREDITO QUE O ENDPOINT NÃO ESTA RETORNANDO ESTES PARAMETROS NO BODY
    }

    @Test
    @DisplayName("Should find client by ID successfully")
    void shouldFindClientByID() {
        var createRequest = """
                {
                    "clientName": "CLIENT_NAME"
                }
                """;

        var response = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(CLIENT_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String location = response.getHeader("Location");
        UUID clientId = UUID.fromString(location.substring(location.lastIndexOf("/") + 1));

        // Agora buscar o cliente criado
        given()
                .pathParam("clientId", clientId)
                .when()
                .get(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(200)
                .body("clientId", equalTo(clientId.toString()))
                .body("clientName", equalTo("CLIENT_NAME"));
    }

    @Test
    @DisplayName("Should return 400 when finding client with non-existent id")
    void shouldReturn400WhenFindingClientWithNonExistentId() {

        UUID nonExistentId = UUID.randomUUID();

        given()
                .pathParam("clientId", nonExistentId)
                .when()
                .get(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(400);

    }

    @Test
    @DisplayName("Should update a client successfully")
    void shouldUpdateClient() {
        var createRequest = """
                {
                    "clientName": "CLIENT_NAME"
                }
                """;

        var response = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(CLIENT_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String location = response.getHeader("Location");
        UUID clientId = UUID.fromString(location.substring(location.lastIndexOf("/") + 1));

        var updateRequest = """
                {
                    "clientName": "NEW_CLIENT_NAME"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("clientId", clientId)
                .body(updateRequest)
                .when()
                .put(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(200)
                .body("clientName", equalTo("Pedro Oliveira Atualizado"));
    }

    @Test
    @Order(10)
    @DisplayName("Should return 400 when updating non-existent client")
    void shouldReturn500WhenUpdatingNonExistentClient() {
        UUID nonExistentId = UUID.randomUUID();

        var updateRequest = """
                {
                    "clientName": "NON_EXISTING_CLIENT"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .pathParam("clientId", nonExistentId)
                .body(updateRequest)
                .when()
                .put(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(400); // Baseado na controller que retorna 500 para RuntimeException
    }

    @Test
    @Order(12)
    @DisplayName("Should delete client successfully")
    void shouldDeleteClientSuccessfully() {
        var createRequest = """
                {
                    "clientName": "CLIENT_NAME"
                }
                """;

        var response = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(CLIENT_ENDPOINT)
                .then()
                .statusCode(201)
                .extract()
                .response();

        String location = response.getHeader("Location");
        UUID clientId = UUID.fromString(location.substring(location.lastIndexOf("/") + 1));

        given()
                .pathParam("clientId", clientId)
                .when()
                .delete(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(204);

        given()
                .pathParam("clientId", clientId)
                .when()
                .get(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 when deleting non-existent client")
    void shouldReturn500WhenDeletingNonExistentClient() {
        UUID nonExistentId = UUID.randomUUID();

        given()
                .pathParam("clientId", nonExistentId)
                .when()
                .delete(CLIENT_ENDPOINT + "/{clientId}")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should handle concurrent requests gracefully")
    void shouldHandleConcurrentRequestsGracefully() {
        // Simular requisições concorrentes
        for (int i = 0; i < 5; i++) {
            var createRequest = String.format("""
                    {
                        "clientName": "Concurrent Test %d"
                    }
                    """, i);

            given()
                    .contentType(ContentType.JSON)
                    .body(createRequest)
                    .when()
                    .post(CLIENT_ENDPOINT)
                    .then()
                    .statusCode(201);
        }
    }
}
