package com.stocksense.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @MappedSuperclass
public abstract class BusinessParty extends BaseEntity {
    @Column(nullable=false, length=160) private String name;
    @Column(length=190) private String email;
    @Column(length=40) private String phone;
    @Column(length=500) private String address;
}
