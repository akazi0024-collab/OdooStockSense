package com.stocksense.repository;
import com.stocksense.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface StockRepository extends JpaRepository<Stock,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from Stock s where s.product.id=:productId and s.location.id=:locationId") Optional<Stock> lockForUpdate(@Param("productId") Long productId,@Param("locationId") Long locationId);
    List<Stock> findByLocationId(Long locationId);
    List<Stock> findByProductId(Long productId);
    List<Stock> findByLocationWarehouseId(Long warehouseId);
    Optional<Stock> findByProductIdAndLocationId(Long productId,Long locationId);
}
