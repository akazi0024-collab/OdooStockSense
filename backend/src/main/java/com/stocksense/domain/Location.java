package com.stocksense.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="locations", uniqueConstraints=@UniqueConstraint(columnNames={"warehouse_id","code"}))
@Getter @Setter @NoArgsConstructor
public class Location extends BaseEntity {
    @Column(nullable=false, length=80) private String name;
    @Column(nullable=false, length=40) private String code;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="warehouse_id") private Warehouse warehouse;
}
