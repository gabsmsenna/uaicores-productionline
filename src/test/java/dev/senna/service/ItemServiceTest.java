package dev.senna.service;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.response.ListItemProductionLineResponse;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @DisplayName("Should return a list with the items on the production line")
    class listProduction {

        @Test
        @DisplayName("Should return a list of items successfully when exists items")
        void shouldListProductionWhenExistsItems() {

            // Arrange
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
            mockItem3.setId(567L);
            mockItem3.setName("ITEM_NAME_3");
            mockItem3.setMaterial("MATERIAL");
            mockItem3.setOrder(null);
            mockItem3.setQuantity(1000);
            mockItem3.setStatus(ItemStatus.EM_SILK);
            mockItem3.setImage("IMG_URL");

            var allowedItemStatus = returnAllowedItemStatus();
            int page = 0;
            int pageSize = 10;

            @SuppressWarnings("unchecked")
            PanacheQuery<ItemEntity> panacheQueryMock = mock(PanacheQuery.class);

            when(itemRepository.find("itemStatus in ?1", allowedItemStatus)).thenReturn(panacheQueryMock);

            when(panacheQueryMock.page(page, pageSize)).thenReturn(panacheQueryMock);

            when(panacheQueryMock.list()).thenReturn(List.of(mockItem1, mockItem2, mockItem3));

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

            assertEquals("ITEM_NAME_2", result.get(1).name());
            assertEquals("MATERIAL", result.get(1).material());
            assertEquals(ItemStatus.ACABAMENTO, result.get(1).itemStatus());
            assertEquals("IMG_URL", result.get(1).image());
            assertEquals(1000, result.get(1).quantity());
            assertEquals(1L, result.get(1).orderId());

            assertEquals("ITEM_NAME_3", result.get(2).name());
            assertEquals("MATERIAL", result.get(2).material());
            assertEquals(ItemStatus.EM_SILK, result.get(2).itemStatus());
            assertEquals("IMG_URL", result.get(2).image());
            assertEquals(1000, result.get(2).quantity());
            assertNull(result.get(2).orderId());
        }

        @Test
        @DisplayName("Should return a list with just one item if ")
        void shouldListProductionWhenExistsAnUniqueItem() {

            // Arrange
            OrderEntity mockOrder = new OrderEntity();
            mockOrder.setId(1L);

            ItemEntity mockItem = new ItemEntity();
            mockItem.setId(123L);
            mockItem.setName("ITEM_NAME");
            mockItem.setMaterial("MATERIAL");
            mockItem.setOrder(mockOrder);
            mockItem.setQuantity(1000);
            mockItem.setStatus(ItemStatus.IMPRESSO);
            mockItem.setImage("IMG_URL");

            var allowedItemStatus = returnAllowedItemStatus();

            int page = 0;
            int pageSize = 10;

            @SuppressWarnings("unchecked")
            PanacheQuery<ItemEntity> panacheQueryMock = mock(PanacheQuery.class);

            when(itemRepository.find("itemStatus in ?1", allowedItemStatus)).thenReturn(panacheQueryMock);

            when(panacheQueryMock.page(page, pageSize)).thenReturn(panacheQueryMock);

            when(panacheQueryMock.list()).thenReturn(List.of(mockItem));

            // Act
            List<ListItemProductionLineResponse> result = itemService.listProduction(0, 10);

            // Assert
            assertNotNull(result);

            assertEquals(1, result.size(), "The list should contain exactly one element");

            assertEquals("ITEM_NAME", result.get(0).name());
            assertEquals("MATERIAL", result.get(0).material());
            assertEquals(ItemStatus.IMPRESSO, result.get(0).itemStatus());
            assertEquals("IMG_URL", result.get(0).image());
            assertEquals(1000, result.get(0).quantity());
            assertEquals(1L, result.get(0).orderId());
        }

        @Test
        @DisplayName("Should return an empty list when no items match the allowed item status")
        void shouldReturnEmptyListWhenNoItemsMatchTheAllowedItemStatus() {

            // Arrange
            int page = 10;
            int pageSize = 10;

            @SuppressWarnings("unchecked")
            PanacheQuery<ItemEntity> panacheQueryMock = mock(PanacheQuery.class);

            when(itemRepository.find("itemStatus in ?1", returnAllowedItemStatus())).thenReturn(panacheQueryMock);
            when(panacheQueryMock.page(page, pageSize)).thenReturn(panacheQueryMock);
            when(panacheQueryMock.list()).thenReturn(Collections.emptyList());

            // Act
            List<ListItemProductionLineResponse> result = itemService.listProduction(page, pageSize);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "The list should be empty");

        }

        List<ItemStatus> returnAllowedItemStatus() {

            return List.of(
                    ItemStatus.IMPRESSO,
                    ItemStatus.ENCARTELADO,
                    ItemStatus.EM_SILK,
                    ItemStatus.CHAPADO,
                    ItemStatus.VERSO_PRONTO,
                    ItemStatus.ACABAMENTO
            );
        }
    }

    @Nested
    @DisplayName("Should assign a the order field of an item")
    class assignOrder {

        @Test
        @DisplayName("Should assign the order field of an item successfully when the item exists")
        void shouldAssignOrderFieldOfAnItemSuccessfullyWhenItemExists() {
            
        }
    }



}