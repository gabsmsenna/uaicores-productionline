package dev.senna.service;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.response.ListItemProductionLineResponse;
import dev.senna.model.entity.ClientEntity;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class ItemServiceTest {

    @Inject
    ItemService itemService;

    @InjectMock
    ItemRepository itemRepository;

    @InjectMock
    OrderRepository orderRepository;

    @InjectMock
    PanacheQuery<ItemEntity> panacheQuery;

    @org.junit.jupiter.api.Nested
    @DisplayName("addItem() tests")
    class AddItemTests {

        @Test
        @DisplayName("Should create an item associated of an order when orderId is valid")
        void shouldCreateAnItemAssociatedOfAnOrderWhenOrderIdIsValid() {

            // Arrange
            long orderId = 1L;
            var dummyDto = new AddItemRequestDto("ITEM_NAME", 1000, "MATERIAL", "IMG_URL", ItemStatus.IMPRESSO, orderId);
            var expectedItemId = 123L;

            // Configurando mock de OrderRepository para encontrar um pedido
            var associatedOrder = new OrderEntity();
            associatedOrder.setId(orderId);

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(associatedOrder));

            // Configurando o mock do ItemRepository para simular a geração de ID
            doAnswer(invocationOnMock -> {
                ItemEntity persistedItem = invocationOnMock.getArgument(0);
                persistedItem.setId(expectedItemId);
                return null;
            }).when(itemRepository).persist(any(ItemEntity.class));

            ArgumentCaptor<ItemEntity> itemEntityArgumentCaptor = ArgumentCaptor.forClass(ItemEntity.class);

            // Act
            Long returnedId = itemService.addItem(dummyDto);

            // Assert
            // Verificando retorno do método
            assertNotNull(returnedId);
            assertEquals(expectedItemId, returnedId);

            //Verificação das interações com os mocks
            verify(orderRepository).findByIdOptional(orderId);
            verify(itemRepository).persist(itemEntityArgumentCaptor.capture());
        }

        @Test
        @DisplayName("Should create an item without association to an order when orderId is null")
        void shouldCreateAnItemWithoutAssociationToAnOrderWhenOrderIdIsNull() {

            // Arrange
            var dummyDtoNoOrder = new AddItemRequestDto("ITEM_NAME_2", 1000, "MATERIAL", "IMG_URL_2", ItemStatus.IMPRESSO, null);
            var expectedItemId = 456L;

            doAnswer(invocationOnMock -> {
                ItemEntity persistedItem = invocationOnMock.getArgument(0);
                persistedItem.setId(expectedItemId);
                return null;
            }).when(itemRepository).persist(any(ItemEntity.class));

            ArgumentCaptor<ItemEntity> itemEntityArgumentCaptor = ArgumentCaptor.forClass(ItemEntity.class);

            // Act
            Long returnedId = itemService.addItem(dummyDtoNoOrder);

            // Assert
            assertNotNull(returnedId);
            assertEquals(expectedItemId, returnedId);

            // Verificar que repositorio de pedido NUNCA será chamado
            verify(orderRepository, never()).findByIdOptional(anyLong());

            verify(itemRepository).persist(itemEntityArgumentCaptor.capture());

            ItemEntity capturedItem = itemEntityArgumentCaptor.getValue();
            assertEquals(dummyDtoNoOrder.name(), capturedItem.getName());
            assertNull(capturedItem.getOrder(), "Order should be null");
        }
    }

    @Nested
    @DisplayName("Should list ")
    class listProduction {

        @Test
        @DisplayName("Should return a list of items successfully when exists items")
        void shouldListProductionWhenExistsItems() {

             // Arrange
            Integer page = 0;
            Integer pageSize = 10;

            OrderEntity mockOrder = new OrderEntity();
            mockOrder.setId(1L);

            ItemEntity mockItem1 = new ItemEntity();
            mockItem1.setId(123L);
            mockItem1.setName("ITEM_NAME");
            mockItem1.setMaterial("MATERIAL");
            mockItem1.setOrder(mockOrder);
            mockItem1.setQuantity(1000);
            mockItem1.setStatus(ItemStatus.IMPRESSO);
            mockItem1.setImage("IMG_URL");

            ItemEntity mockItem2 = new ItemEntity();
            mockItem2.setId(456L);
            mockItem2.setName("ITEM_NAME_2");
            mockItem2.setMaterial("MATERIAL");
            mockItem2.setOrder(mockOrder);
            mockItem2.setQuantity(1000);
            mockItem2.setStatus(ItemStatus.ACABAMENTO);
            mockItem2.setImage("IMG_URL");

            ItemEntity mockItem3 = new ItemEntity();
            mockItem2.setId(567L);
            mockItem2.setName("ITEM_NAME_3");
            mockItem2.setMaterial("MATERIAL");
            mockItem2.setOrder(null);
            mockItem2.setQuantity(1000);
            mockItem2.setStatus(ItemStatus.EM_SILK);
            mockItem2.setImage("IMG_URL");

            when(panacheQuery.list()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

            // Act
            List<ListItemProductionLineResponse> result = itemService.listProduction(0, 10);

            // Assert
            assertEquals(3, result.size());

            assertEquals("ITEM_NAME", result.get(0).name());
            assertEquals("MATERIAL", result.get(0).material());
            assertEquals(ItemStatus.IMPRESSO, result.get(0).itemStatus());
            assertEquals("IMG_URL", result.get(0).image());
            assertEquals(1000, result.get(0).quantity());
            assertEquals(1L, result.get(0).orderId());

            assertEquals("ITEM_NAME_2", result.get(0).name());
            assertEquals("MATERIAL", result.get(0).material());
            assertEquals(ItemStatus.ACABAMENTO, result.get(0).itemStatus());
            assertEquals("IMG_URL", result.get(0).image());
            assertEquals(1000, result.get(0).quantity());
            assertEquals(1L, result.get(0).orderId());

            assertEquals("ITEM_NAME_3", result.get(0).name());
            assertEquals("MATERIAL", result.get(0).material());
            assertEquals(ItemStatus.EM_SILK, result.get(0).itemStatus());
            assertEquals("IMG_URL", result.get(0).image());
            assertEquals(1000, result.get(0).quantity());
            assertNull(result.get(0).orderId());

        }
    }



//    MINHA PRIMEIRA VERSÃO DO TESTE
//    @Test
//    @DisplayName("Should create an item to the production line")
//    void addItem() {
//
//        // Arrange
//        var dummyItemDto = new AddItemRequestDto("ITEM_NAME", 1000, "MATERIAL", "IMG_URL", ItemStatus.IMPRESSO, 1L);
//        ArgumentCaptor<ItemEntity> itemEntityCaptor = ArgumentCaptor.forClass(ItemEntity.class);
//
//        // Act
//       var itemIdReturned =  itemService.addItem(dummyItemDto);
//
//        // Assert
//        verify(itemRepository).persist(itemEntityCaptor.capture());
//        assertNotNull(itemIdReturned);
//    }
}