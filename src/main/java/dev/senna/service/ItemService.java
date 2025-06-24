package dev.senna.service;

import dev.senna.controller.dto.ListProductionLineResponse;
import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.controller.dto.request.AssignOrderToItemRequestDto;
import dev.senna.exception.ItemNotFoundException;
import dev.senna.exception.OrderNotFoundException;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.enums.ItemStatus;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

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

    public List<ListProductionLineResponse> listProduction(Integer page, Integer pageSize) {

        List<ItemStatus> allowedItemStatuses = List.of(
                ItemStatus.IMPRESSO,
                ItemStatus.ENCARTELADO,
                ItemStatus.EM_SILK,
                ItemStatus.CHAPADO
        );


        var items = itemRepository.find("itemStatus in ?1", allowedItemStatuses)
                .page(page, pageSize)
                .list();

        return items.stream()
                .map(itemEntity -> new ListProductionLineResponse(
                        itemEntity.getName(),
                        itemEntity.getQuantity(),
                        itemEntity.getSaleQuantity(),
                        itemEntity.getMaterial(),
                        itemEntity.getImage(),
                        itemEntity.getStatus(),
                        itemEntity.getOrder()
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
}
