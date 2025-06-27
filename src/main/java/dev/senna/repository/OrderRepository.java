package dev.senna.repository;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<OrderEntity, Long> {

    public List<OrderEntity> listOrdersInProduction(int page, int pageSize) {
        return find("status", OrderStatus.PRODUCAO)
                .page(Page.of(page, pageSize))
                .list();
    }
}
