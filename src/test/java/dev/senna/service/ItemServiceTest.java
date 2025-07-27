package dev.senna.service;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.response.ListItemProductionLineResponse;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// 1. Substituímos @QuarkusTest pela anotação que ativa o Mockito para JUnit 5
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    // 2. @InjectMocks do Mockito cria a instância do serviço e injeta os mocks
    @InjectMocks
    private ItemService itemService;

    // 3. @Mock do Mockito cria as dependências falsas (mocks)
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Nested
    @DisplayName("addItem() tests")
    class AddItemTests {

        @Test
        @DisplayName("Should create an item associated of an order when orderId is valid")
        void shouldCreateAnItemAssociatedOfAnOrderWhenOrderIdIsValid() {
            // Arrange
            long orderId = 1L;
            var dummyDto = new AddItemRequestDto("ITEM_NAME", 1000, "MATERIAL", "IMG_URL", ItemStatus.IMPRESSO, orderId);
            var expectedItemId = 123L;

            var associatedOrder = new OrderEntity();
            associatedOrder.setId(orderId);
            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(associatedOrder));

            // Simula a persistência e a atribuição de um ID
            doAnswer(invocationOnMock -> {
                ItemEntity persistedItem = invocationOnMock.getArgument(0);
                persistedItem.setId(expectedItemId);
                return null;
            }).when(itemRepository).persist(any(ItemEntity.class));

            // Act
            Long returnedId = itemService.addItem(dummyDto);

            // Assert
            assertEquals(expectedItemId, returnedId);
            verify(orderRepository).findByIdOptional(orderId);
            verify(itemRepository).persist(any(ItemEntity.class));
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
            assertEquals(expectedItemId, returnedId);
            verify(orderRepository, never()).findByIdOptional(anyLong());
            verify(itemRepository).persist(itemEntityArgumentCaptor.capture());

            ItemEntity capturedItem = itemEntityArgumentCaptor.getValue();
            assertEquals(dummyDtoNoOrder.name(), capturedItem.getName());
            assertNull(capturedItem.getOrder(), "Order should be null");
        }
    }

    // O teste para a query do Panache continua válido, pois estamos mockando a API dele
    // e não dependendo da sua implementação real.
    @Nested
    @DisplayName("listProduction() tests")
    class ListProductionTests {

        private final List<ItemStatus> allowedStatus = List.of(
                ItemStatus.IMPRESSO, ItemStatus.ENCARTELADO, ItemStatus.EM_SILK,
                ItemStatus.CHAPADO, ItemStatus.VERSO_PRONTO, ItemStatus.ACABAMENTO
        );

        @Test
        @DisplayName("Should return a list of items successfully when items exist")
        void shouldListProductionWhenItemsExist() {
            // Arrange
            var mockItem1 = new ItemEntity();
            mockItem1.setId(123L);
            mockItem1.setName("ITEM_NAME");
            mockItem1.setStatus(ItemStatus.IMPRESSO);

            var mockItem2 = new ItemEntity();
            mockItem2.setId(456L);
            mockItem2.setName("ITEM_NAME_2");
            mockItem2.setStatus(ItemStatus.ACABAMENTO);

            PanacheQuery<ItemEntity> panacheQueryMock = mock(PanacheQuery.class);
            when(itemRepository.find("itemStatus in ?1", allowedStatus)).thenReturn(panacheQueryMock);
            when(panacheQueryMock.page(0, 10)).thenReturn(panacheQueryMock);
            when(panacheQueryMock.list()).thenReturn(List.of(mockItem1, mockItem2));

            // Act
            List<ListItemProductionLineResponse> result = itemService.listProduction(0, 10);

            // Assert
            assertEquals(2, result.size());
            assertEquals("ITEM_NAME", result.get(0).name());
            assertEquals("ITEM_NAME_2", result.get(1).name());
            verify(itemRepository).find("itemStatus in ?1", allowedStatus);
            verify(panacheQueryMock).page(0, 10);
            verify(panacheQueryMock).list();
        }

        @Test
        @DisplayName("Should return an empty list when no items match")
        void shouldReturnEmptyListWhenNoItemsMatch() {
            // Arrange
            PanacheQuery<ItemEntity> panacheQueryMock = mock(PanacheQuery.class);
            when(itemRepository.find("itemStatus in ?1", allowedStatus)).thenReturn(panacheQueryMock);
            when(panacheQueryMock.page(0, 10)).thenReturn(panacheQueryMock);
            when(panacheQueryMock.list()).thenReturn(Collections.emptyList());

            // Act
            List<ListItemProductionLineResponse> result = itemService.listProduction(0, 10);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("assignOrder() tests")
    class AssignOrderTests {

        @Test
        @DisplayName("Should assign the order to an item successfully when both exist")
        void shouldAssignOrderToAnItemSuccessfullyWhenBothExist() {
            // Arrange
            var itemId = 1L;
            var orderId = 2L;
            var assignOrderDto = new AssignOrderToItemRequestDto(orderId);

            var associatedOrder = new OrderEntity();
            associatedOrder.setId(orderId);

            var itemToAssignOrder = new ItemEntity();
            itemToAssignOrder.setId(itemId);

            when(orderRepository.findByIdOptional(orderId)).thenReturn(Optional.of(associatedOrder));
            when(itemRepository.findByIdOptional(itemId)).thenReturn(Optional.of(itemToAssignOrder));

            ArgumentCaptor<ItemEntity> itemCaptor = ArgumentCaptor.forClass(ItemEntity.class);

            // Act
            itemService.assignOrder(assignOrderDto, itemId);

            // Assert
            verify(itemRepository).persist(itemCaptor.capture());
            ItemEntity capturedItem = itemCaptor.getValue();

            assertNotNull(capturedItem.getOrder());
            assertEquals(orderId, capturedItem.getOrder().getId());
        }
    }
}