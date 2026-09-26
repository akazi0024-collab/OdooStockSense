package com.stocksense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="warehouses")
@Getter @Setter @NoArgsConstructor
public class Warehouse extends BaseEntity {
    @Column(nullable=false, unique=true, length=120) private String name;
    @Column(nullable=false, unique=true, length=40) private String code;
    @Column(length=500) private String address;
}
