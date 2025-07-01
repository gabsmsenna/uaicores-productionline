package dev.senna.service;

import dev.senna.controller.dto.response.ListItemProductionLineResponse;
import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.controller.dto.request.UpdateItemRequestDto;
import dev.senna.exception.ItemNotFoundException;
import dev.senna.exception.InvalidEditParameterException;
import dev.senna.exception.OrderNotFoundException;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ItemService {

    @Inject
    private ItemRepository itemRepository;

    @Inject
    private OrderRepository orderRepository;

    public Long addItem(AddItemRequestDto reqDto) {

        var item = new ItemEntity();

        if (reqDto.orderId() == null) {
            item.setOrder(null);
        } else {
            var order = orderRepository.findByIdOptional(reqDto.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(reqDto.orderId()));
            item.setOrder(order);
        }

      item.setName(reqDto.name());
      item.setSaleQuantity(reqDto.saleQuantity());
      item.setMaterial(reqDto.material());
      item.setImage(reqDto.image());
      item.setStatus(ItemStatus.IMPRESSO);

      itemRepository.persist(item);

      return item.getId();
    }

    public ItemEntity findItemById(Long itemId) {

        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return item;
    }

    public List<ListItemProductionLineResponse> listProduction(Integer page, Integer pageSize) {

        List<ItemStatus> allowedItemStatus = List.of(
                ItemStatus.IMPRESSO,
                ItemStatus.ENCARTELADO,
                ItemStatus.EM_SILK,
                ItemStatus.CHAPADO
        );


        var items = itemRepository.find("itemStatus in ?1", allowedItemStatus)
                .page(page, pageSize)
                .list();

        return items.stream()
                .map(itemEntity -> new ListItemProductionLineResponse(
                        itemEntity.getName(),
                        itemEntity.getQuantity(),
                        itemEntity.getSaleQuantity(),
                        itemEntity.getMaterial(),
                        itemEntity.getImage(),
                        itemEntity.getStatus(),
                        itemEntity.getOrder() != null ? itemEntity.getOrder().getId() : null
                )).toList();
    }

    public void assignOrder(AssignOrderToItemRequestDto reqDto, Long itemId) {

        var order = orderRepository.findByIdOptional(reqDto.orderId())
                .orElseThrow(() -> new OrderNotFoundException(reqDto.orderId()));

        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        item.setOrder(order);

        itemRepository.persist(item);
    }

    public void updateItem(Long itemId, UpdateItemRequestDto reqDto) {
        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        boolean isAnyFieldUpdated = false;

        if (reqDto.name() != null) {
            item.setName(reqDto.name());
            isAnyFieldUpdated = true;
        }
        if (reqDto.quantity() != null) {
            item.setQuantity(reqDto.quantity());
            isAnyFieldUpdated = true;
        }
        if (reqDto.saleQuantity() != null) {
            item.setSaleQuantity(reqDto.saleQuantity());
            isAnyFieldUpdated = true;
        }
        if (reqDto.material() != null) {
            item.setMaterial(reqDto.material());
            isAnyFieldUpdated = true;
        }
        if (reqDto.image() != null) {
            item.setImage(reqDto.image());
            isAnyFieldUpdated = true;
        }
        if (reqDto.itemStatus() != null) {
            item.setStatus(reqDto.itemStatus());
            isAnyFieldUpdated = true;
        }

        if (!isAnyFieldUpdated) {
            throw new InvalidEditParameterException();
        }

        itemRepository.persist(item);
    }

    public List<ListItemProductionLineResponse> findByStatus(ItemStatus status) {

        Objects.requireNonNull(status, "ItemStatus must not be null");

        var items = itemRepository.findByStatus(status);

        return items.stream()
                .map(itemEntity -> new ListItemProductionLineResponse(
                        itemEntity.getName(),
                        itemEntity.getQuantity(),
                        itemEntity.getSaleQuantity(),
                        itemEntity.getMaterial(),
                        itemEntity.getImage(),
                        itemEntity.getStatus(),
                        itemEntity.getOrder() != null ? itemEntity.getOrder().getId() : null
                )).toList();
    }
}
