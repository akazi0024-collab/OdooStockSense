package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.stocksense.domain.DomainTypes.DocumentStatus;

@Entity @Table(name="receipts") @Getter @Setter @NoArgsConstructor
public class Receipt extends BaseEntity {
    @Column(nullable=false, unique=true, length=40) private String reference;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="location_id") private Location location;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="supplier_id") private Supplier supplier;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private DocumentStatus status=DocumentStatus.DRAFT;
    @OneToMany(mappedBy="receipt", cascade=CascadeType.ALL, orphanRemoval=true) private List<ReceiptItem> items=new ArrayList<>();
}
