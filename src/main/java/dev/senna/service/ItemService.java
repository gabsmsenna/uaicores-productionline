package dev.senna.service;

import dev.senna.controller.ClientController;
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
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class ItemService {

    @Inject
    private ItemRepository itemRepository;

    @Inject
    private OrderRepository orderRepository;

    @Inject
    SecurityIdentity identity;

    private static final Logger log = LoggerFactory.getLogger(ClientController.class);

    public Long addItem(AddItemRequestDto reqDto) {

        log.debug("Creating item");

        var item = new ItemEntity();

        if (reqDto.orderId() == null) {
            log.info("Order id not provided");
            item.setOrder(null);
        } else {
            var order = orderRepository.findByIdOptional(reqDto.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(reqDto.orderId()));
            log.info("Item associated to order {} ", reqDto.orderId());
            item.setOrder(order);
        }

      item.setName(reqDto.name());
      item.setSaleQuantity(reqDto.saleQuantity());
      item.setQuantity(reqDto.saleQuantity());
      item.setMaterial(reqDto.material());
      item.setImage(reqDto.image());
      item.setStatus(ItemStatus.IMPRESSO);

      itemRepository.persist(item);

      log.info("Created Item {}", item);

      return item.getId();
    }

    public ItemEntity findItemById(Long itemId) {

        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
        log.info("Item founded with success!");
        return item;
    }

    public List<ListItemProductionLineResponse> listProduction(Integer page, Integer pageSize) {

        List<ItemStatus> allowedItemStatus = List.of(
                ItemStatus.IMPRESSO,
                ItemStatus.ENCARTELADO,
                ItemStatus.EM_SILK,
                ItemStatus.CHAPADO,
                ItemStatus.VERSO_PRONTO,
                ItemStatus.ACABAMENTO
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
        log.debug("Assigning order ID {} to item ID {}", reqDto.orderId(), itemId);

        var order = orderRepository.findByIdOptional(reqDto.orderId())
                .orElseThrow(() -> new OrderNotFoundException(reqDto.orderId()));

        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        item.setOrder(order);

        itemRepository.persist(item);

        log.info("Item ID {} foi atribuído com sucesso ao pedido ID {}", item.getId(), order.getId());
    }

    public void updateItem(Long itemId, UpdateItemRequestDto reqDto) {
        var item = itemRepository.findByIdOptional(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        orderRepository.findByIdOptional(reqDto.orderId())
                .orElseThrow(() -> new OrderNotFoundException(reqDto.orderId()));

        var roles = identity.getRoles();

        boolean updated = false;

        if (roles.contains("ADMIN")) {
            updated = applyAdminUpdates(item, reqDto);
        } else if (roles.contains("OFFICER")) {
            updated = applyOfficerUpdates(item, reqDto);
        } else {
            log.warn("Usuário sem permissão tentou atualizar item ID: {}", itemId);
            throw new ForbiddenException("Você não possui permissão para atualizar este item");
        }

        if (!updated) {
            log.error("Nenhum campo válido para update foi informado no item ID: {}", itemId);
            throw new InvalidEditParameterException();
        }

        log.info("Item ID {} atualizado com sucesso pelo usuário {} - Novos valores: {}",
                itemId, identity.getPrincipal().getName(), item);
    }

    private boolean applyOfficerUpdates(ItemEntity item, UpdateItemRequestDto reqDto) {

        boolean updated = false;

        if (reqDto.quantity() != null) {
            item.setQuantity(reqDto.quantity());
            updated = true;
        }
        if (reqDto.itemStatus() != null) {
            item.setStatus(reqDto.itemStatus());
            updated = true;
        }

        return updated;
    }

    private boolean applyAdminUpdates(ItemEntity item, UpdateItemRequestDto reqDto) {

        boolean updated = false;

        if (reqDto.name() != null) {
            item.setName(reqDto.name());
            updated = true;
        }
        if (reqDto.quantity() != null) {
            item.setQuantity(reqDto.quantity());
            updated = true;
        }
        if (reqDto.saleQuantity() != null) {
            item.setSaleQuantity(reqDto.saleQuantity());
            updated = true;
        }
        if (reqDto.material() != null) {
            item.setMaterial(reqDto.material());
            updated = true;
        }
        if (reqDto.image() != null) {
            item.setImage(reqDto.image());
            updated = true;
        }
        if (reqDto.itemStatus() != null) {
            item.setStatus(reqDto.itemStatus());
            updated = true;
        }

        return updated;
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
