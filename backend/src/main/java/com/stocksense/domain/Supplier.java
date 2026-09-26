package com.stocksense.domain;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity @Table(name="suppliers") @Getter @Setter @NoArgsConstructor
public class Supplier extends BusinessParty {}
