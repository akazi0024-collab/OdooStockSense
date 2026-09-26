package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import com.stocksense.domain.DomainTypes.DocumentStatus;

@Entity @Table(name="deliveries") @Getter @Setter @NoArgsConstructor
public class Delivery extends BaseEntity {
    @Column(nullable=false, unique=true, length=40) private String reference;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="location_id") private Location location;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id") private Customer customer;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private DocumentStatus status=DocumentStatus.DRAFT;
    @OneToMany(mappedBy="delivery", cascade=CascadeType.ALL, orphanRemoval=true) private List<DeliveryItem> items=new ArrayList<>();
}
