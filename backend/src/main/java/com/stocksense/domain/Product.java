package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="products", uniqueConstraints=@UniqueConstraint(columnNames="sku"))
@Getter @Setter @NoArgsConstructor
public class Product extends BaseEntity {
    @Column(nullable=false, length=80) private String sku;
    @Column(nullable=false, length=180) private String name;
    @Column(length=1000) private String description;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="category_id") private Category category;
    @Column(nullable=false, precision=14, scale=2) private BigDecimal unitPrice = BigDecimal.ZERO;
    @Column(nullable=false) private int lowStockThreshold = 5;
    @Column(nullable=false) private boolean active = true;
}
