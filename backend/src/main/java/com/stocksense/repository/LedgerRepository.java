package com.stocksense.repository;
import com.stocksense.domain.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface LedgerRepository extends JpaRepository<StockLedger,Long> {
    List<StockLedger> findAllByOrderByOccurredAtDesc(Pageable pageable);
    List<StockLedger> findByStockLocationIdOrderByOccurredAtDesc(Long locationId, Pageable pageable);
}
