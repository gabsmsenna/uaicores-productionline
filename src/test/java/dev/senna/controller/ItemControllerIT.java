package dev.senna.controller;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.request.UpdateItemRequestDto;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.model.enums.Material;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemControllerIT {

    @Inject
    EntityManager entityManager;

    private Long existingOrderId;
    private Long existingItemId;

    @BeforeEach
    @Transactional
    void setUp() {

        entityManager.createQuery("DELETE FROM ItemEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM OrderEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM ClientEntity").executeUpdate();

        ClientEntity client = new ClientEntity();
        client.setClientId(UUID.randomUUID());
        client.setClientName("CLIENT_NAME");
        entityManager.persist(client);

        OrderEntity order = new OrderEntity();
        order.setClient(client);
        order.setSaleDate(LocalDate.now());
        order.setDeliveryDate(LocalDate.now().plusDays(10));
        entityManager.persist(order);
        existingOrderId = order.getId();

        ItemEntity item = new ItemEntity();
        item.setName("ITEM_NAME");
        item.setSaleQuantity(1000);
        item.setMaterial(Material.ADESIVO);
        item.setImage("IMG_URL");
        item.setStatus(ItemStatus.IMPRESSO);
        entityManager.persist(item);
        existingItemId = item.getId();
    }

    @Test
    @DisplayName("POST /item - Deve criar um item com sucesso sem um pedido associado")
    void createItem_Success_WithoutOrder() {
        var requestDto = new AddItemRequestDto("ITEM_WITHOUT_ORDER", 500, Material.ELETROSTATICO, "new.jpg", null);

        given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/item")
                .then()
                .statusCode(201)
                .header("Location", containsString("/item"));
    }

    @Test
    @DisplayName("POST /item - Deve criar um item com sucesso associado a um pedido existente")
    void createItem_Success_WithExistingOrder() {
        var requestDto = new AddItemRequestDto("ITEM_WITH_ORDER_ASSOCIATED", 2000, Material.BRANCO_FOSCO, "paper.jpg", existingOrderId);

        String newItemIdStr = given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/item")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString();

        ItemEntity createdItem = entityManager.find(ItemEntity.class, Long.parseLong(newItemIdStr));
        assertNotNull(createdItem);
        assertNotNull(createdItem.getOrder());
        assertEquals(existingOrderId, createdItem.getOrder().getId());
    }

    @Test
    @DisplayName("POST /item - Should fail when trying to create an item associated to an inexistent order")
    void createItem_Fail_WithNonExistentOrder() {
        long nonExistentOrderId = 999L;
        var requestDto = new AddItemRequestDto("ITEM_WITH_ORDER_INEXISTENT", 200, Material.LONA, "IMG_URL", nonExistentOrderId);

        given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/item")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /item - Should fail to validation violetion (blank name)")
    void createItem_Fail_Validation() {
        var requestDto = new AddItemRequestDto("", 50, Material.ADESIVO, "IMG_URL", null); // Nome em branco

        given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/item")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("PUT /item/{itemId}/order - Should associate an item to an order")
    void assignOrder_Success() {
        var requestDto = new AssignOrderToItemRequestDto(existingOrderId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("itemId", existingItemId)
                .body(requestDto)
                .when()
                .put("/item/{itemId}/order")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("PUT /item/{itemId}/order - Should fail when trying to associate an item with id invalid")
    void assignOrder_Fail_ItemNotFound() {
        long nonExistentItemId = 999L;
        var requestDto = new AssignOrderToItemRequestDto(existingOrderId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("itemId", nonExistentItemId)
                .body(requestDto)
                .when()
                .put("/item/{itemId}/order")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("PUT /item/{itemId} - Should update an item")
    void updateItem_Success() {
        var updateDto = new UpdateItemRequestDto("Nome Atualizado", null, 250, null, null, ItemStatus.EMBALADO, existingOrderId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("itemId", existingItemId)
                .body(updateDto)
                .when()
                .put("/item/{itemId}")
                .then()
                .statusCode(200);

        ItemEntity updatedItem = entityManager.find(ItemEntity.class, existingItemId);
        assertEquals("Nome Atualizado", updatedItem.getName());
        assertEquals(250, updatedItem.getSaleQuantity());
        assertEquals(ItemStatus.EMBALADO, updatedItem.getStatus());
    }

    @Test
    @DisplayName("PUT /item/{itemId} - Should fail if no field were given")
    void updateItem_Fail_NoFieldsToUpdate() {

        var updateDto = new UpdateItemRequestDto(null, null, null, null, null, null, existingOrderId);

        given()
                .contentType(ContentType.JSON)
                .pathParam("itemId", existingItemId)
                .body(updateDto)
                .when()
                .put("/item/{itemId}")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("GET /item/search - Should find items that match the status")
    void searchItemByStatus_Success() {
        given()
                .queryParam("status", ItemStatus.IMPRESSO.name())
                .when()
                .get("/item/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("Item de Teste Inicial"));
    }

    @Test
    @DisplayName("GET /item/search - Should return an empty list to a status without items")
    void searchItemByStatus_Success_NoResults() {
        given()
                .queryParam("status", ItemStatus.EMBALADO.name())
                .when()
                .get("/item/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("GET /item/search - Should fail when status not given")
    void searchItemByStatus_Fail_NullStatus() {
        given()
                .when()
                .get("/item/search")
                .then()
                .statusCode(400);
    }

}