package dev.senna.repository;

import dev.senna.model.entity.ItemEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ItemRepository implements PanacheRepositoryBase<ItemEntity, Long> {
}
