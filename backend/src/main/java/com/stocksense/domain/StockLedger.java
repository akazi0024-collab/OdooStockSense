package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import com.stocksense.domain.DomainTypes.LedgerType;

@Entity @Table(name="stock_ledger", indexes=@Index(columnList="occurred_at")) @Getter @Setter @NoArgsConstructor
public class StockLedger extends BaseEntity {
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="stock_id") private Stock stock;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=30) private LedgerType type;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal quantityDelta;
    @Column(nullable=false, precision=14, scale=3) private BigDecimal balanceAfter;
    @Column(nullable=false, length=60) private String reference;
    @Column(length=180) private String performedBy;
    @Column(name="occurred_at", nullable=false, updatable=false) private Instant occurredAt;
    @PrePersist protected void stamp() { if (occurredAt == null) occurredAt=Instant.now(); }
    @PreUpdate protected void immutableUpdate() { throw new IllegalStateException("Stock ledger entries are immutable"); }
    @PreRemove protected void immutableDelete() { throw new IllegalStateException("Stock ledger entries are immutable"); }
}
