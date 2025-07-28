package dev.senna.model.entity;

import dev.senna.model.enums.ItemStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_item")
public class ItemEntity {

    public ItemEntity(Long id, String name, Integer quantity, Integer saleQuantity, String material, String image, ItemStatus itemStatus, OrderEntity order) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.saleQuantity = saleQuantity;
        this.material = material;
        this.image = image;
        this.itemStatus = itemStatus;
        this.order = order;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String name;

    @Column(name = "item_quantity")
    private Integer quantity;

    @Column(name = "sale_quantity", nullable = false)
    private Integer saleQuantity;

    @Column(name = "material", nullable = false )
    private String material;

    @Column(name = "image")
    private String image;

    @Column(name = "actual_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    public ItemEntity() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSaleQuantity() {
        return saleQuantity;
    }

    public void setSaleQuantity(Integer saleQuantity) {
        this.saleQuantity = saleQuantity;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ItemStatus getStatus() {
        return itemStatus;
    }

    public void setStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
