package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import com.stocksense.domain.DomainTypes.DocumentStatus;

@Entity @Table(name="stock_adjustments") @Getter @Setter @NoArgsConstructor
public class StockAdjustment extends BaseEntity {
    @Column(nullable=false, unique=true, length=40) private String reference;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="location_id") private Location location;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal quantityDelta;
    @Column(length=500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private DocumentStatus status=DocumentStatus.DRAFT;
}
