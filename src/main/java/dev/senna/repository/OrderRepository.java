package dev.senna.repository;

import dev.senna.model.entity.OrderEntity;
import dev.senna.model.enums.OrderStatus;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<OrderEntity, Long> {

    public List<OrderEntity> listOrdersInProduction(int page, int pageSize) {
        return find("status", OrderStatus.PRODUCAO)
                .page(Page.of(page, pageSize))
                .list();
    }

    public PanacheQuery<OrderEntity> findLastSent(int page, int pageSize) {

        Sort sort = Sort.by("deliveryDate", Sort.Direction.Descending)
                .and("id", Sort.Direction.Descending);

        var query = this.find("status = ?1", sort, OrderStatus.POSTADO);

        query.page(Page.of(page, pageSize));

        return query;
    }

    public List<OrderEntity> findByPostedDateBetween(LocalDate start, LocalDate end) {
        return list("postedDate >= ?1 and postedDate <= ?2", start, end);
    }

    public long countByPostedDateBetween(LocalDate start, LocalDate end) {
        return count("postedDate >= ?1 and postedDate <= ?2", start, end);
    }

    public List<OrderEntity> findTop4ByOrderByIdDesc() {
        return find("ORDER BY id DESC")
                .page(0, 4)
                .list();
    }
}
