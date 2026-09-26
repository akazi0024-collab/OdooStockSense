package com.stocksense.repository;
import com.stocksense.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category,Long> { boolean existsByNameIgnoreCase(String name); }
