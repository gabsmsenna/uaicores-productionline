package dev.senna.repository;

import dev.senna.model.entity.ItemEntity;
import dev.senna.model.enums.ItemStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ItemRepository implements PanacheRepositoryBase<ItemEntity, Long> {

    public List<ItemEntity> findByStatus(ItemStatus status) {
        return find("itemStatus", status).list();
    }

}
