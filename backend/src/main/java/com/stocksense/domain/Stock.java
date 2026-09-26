package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="stocks", uniqueConstraints=@UniqueConstraint(columnNames={"product_id","location_id"}))
@Getter @Setter @NoArgsConstructor
public class Stock extends BaseEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="product_id") private Product product;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="location_id") private Location location;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal quantity = BigDecimal.ZERO;
}
