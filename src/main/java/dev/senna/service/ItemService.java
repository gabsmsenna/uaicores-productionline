package dev.senna.service;

import dev.senna.controller.dto.request.AddItemRequestDto;
import dev.senna.exception.ItemAlreadyHasOrder;
import dev.senna.exception.ItemNotFoundException;
import dev.senna.exception.OrderNotFoundException;
import dev.senna.model.entity.ItemEntity;
import dev.senna.model.enums.Status;
import dev.senna.repository.ItemRepository;
import dev.senna.repository.OrderRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
      item.setStatus(Status.IMPRESSO);

      itemRepository.persist(item);

      return item.getId();
    }

    public ItemEntity findItemById(Long itemId) {

        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        return item;
    }
}
