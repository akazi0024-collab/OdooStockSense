package com.stocksense.repository;
import com.stocksense.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product,Long> { boolean existsBySkuIgnoreCase(String sku); }
