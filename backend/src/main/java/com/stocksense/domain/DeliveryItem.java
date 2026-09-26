package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="delivery_items") @Getter @Setter @NoArgsConstructor
public class DeliveryItem extends BaseEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="delivery_id") private Delivery delivery;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal quantity;
}
