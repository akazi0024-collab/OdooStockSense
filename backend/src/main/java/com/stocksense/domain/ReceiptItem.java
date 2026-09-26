package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="receipt_items") @Getter @Setter @NoArgsConstructor
public class ReceiptItem extends BaseEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="receipt_id") private Receipt receipt;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal quantity;
    @Column(precision=14, scale=2) private BigDecimal unitCost;
}
