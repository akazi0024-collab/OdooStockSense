package com.stocksense.repository;
import com.stocksense.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SupplierRepository extends JpaRepository<Supplier,Long> {}
